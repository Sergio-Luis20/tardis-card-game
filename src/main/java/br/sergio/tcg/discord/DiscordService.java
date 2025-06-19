package br.sergio.tcg.discord;

import br.sergio.tcg.InterfaceDriver;
import br.sergio.tcg.discord.slash.SlashCommand;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.SessionManager;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Card;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class DiscordService implements InterfaceDriver, AutoCloseable {

    public static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9áéíóúýàèìòùäëïöüâêîôûãõñç-]{1,32}");

    @Getter
    private static DiscordService instance;

    private JDA jda;
    private Guild guild;
    private TextChannel gameChannel;

    private SessionManager sessionManager;
    private EmbedFactory embedFactory;

    private Map<Class<?>, Function<?, ?>> elementMapperCache;

    private DiscordService(JDA jda, Guild guild, TextChannel gameChannel) {
        this.jda = jda;
        this.guild = guild;
        this.gameChannel = gameChannel;

        sessionManager = new SessionManager(this);
        embedFactory = new EmbedFactory();
        elementMapperCache = new ConcurrentHashMap<>();

        populateMapperCache();
    }

    public void registerListener(EventListener listener) {
        jda.addEventListener(listener);
    }

    public void registerSlashCommands(Collection<SlashCommand> commands, boolean force) {
        var commandMap = commands.stream()
                .map(SlashCommand::slashData)
                .collect(Collectors.toMap(SlashCommandData::getName, Function.identity()));
        if (force) {
            for (var guild : jda.getGuilds()) {
                guild.updateCommands().addCommands(commandMap.values()).queue(null, t -> {
                    log.error("Could not update commands for guild {} (force = true)", guild.getName(), t);
                });
            }
        } else {
            for (var guild : jda.getGuilds()) {
                guild.retrieveCommands().queue(cmds -> {
                    var toBeAdded = new HashSet<SlashCommandData>();
                    long id = jda.getSelfUser().getApplicationIdLong();
                    var names = cmds.stream()
                            .filter(cmd -> cmd.getApplicationIdLong() == id)
                            .map(Command::getName)
                            .collect(Collectors.toSet());
                    commandMap.entrySet().stream()
                            .filter(entry -> !names.contains(entry.getKey()))
                            .map(Entry::getValue)
                            .forEach(toBeAdded::add);
                    if (toBeAdded.isEmpty()) {
                        return;
                    }
                    guild.updateCommands().addCommands(toBeAdded).queue(null, t -> {
                        log.error("Could not update commands for guild {} (force = false)", guild.getName(), t);
                    });
                }, t -> log.error("Failed to fetch commands for guild {}", guild.getName()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T, U> Function<T, U> getMapper(Class<T> elementType) {
        return (Function<T, U>) elementMapperCache.get(elementType);
    }

    @Override
    public DiscordGameSession createSession(Player host, UUID id) {
        if (host instanceof DiscordPlayer dp) {
            return new DiscordGameSession(dp, id);
        } else {
            throw new IllegalArgumentException("Host must be an instance of DiscordPlayer");
        }
    }

    public Optional<DiscordGameSession> findSession(Member member) {
        return sessionManager.getActiveSessions().values().stream()
                .map(DiscordGameSession.class::cast)
                .filter(session -> session.containsMember(member))
                .findAny();
    }

    public Optional<DiscordGameSession> ofHost(Member member) {
        return sessionManager.getActiveSessions().values().stream()
                .map(DiscordGameSession.class::cast)
                .filter(session -> session.getHost().getMember().equals(member))
                .findAny();
    }

    @Override
    public <T> CompletableFuture<List<T>> chooseMany(Player player, String prompt, List<T> options, Class<T> type, boolean allowEmpty) {
        if (options.isEmpty() || options.size() == 1) {
            return CompletableFuture.completedFuture(options);
        }

        if (!(player instanceof DiscordPlayer discordPlayer)) {
            throw new IllegalArgumentException("Not a discord player: " + player.getName());
        }

        String sessionId = UUID.randomUUID().toString();
        Function<T, CardEmbed> mapper = getMapper(type);

        int[] cursor = {0};
        Set<T> selected = new HashSet<>();

        Function<T, List<Button>> makeButtons = (current) -> {
            boolean isSelected = selected.contains(current);

            return List.of(
                    Button.primary(sessionId + ":previous", "⬅️ Anterior"),
                    Button.secondary(sessionId + ":select", isSelected ? "✅ Desmarcar" : "☑️ Selecionar"),
                    Button.primary(sessionId + ":next", "➡️ Próxima"),
                    Button.success(sessionId + ":confirm", "✔️ Confirmar")
            );
        };

        BiConsumer<Message, T> updateMessage = (message, current) -> {
            CardEmbed cardEmbed = mapper.apply(current);

            message.editMessageEmbeds(cardEmbed.embed())
                    .setFiles(FileUpload.fromData(cardEmbed.imageData(), cardEmbed.filename()))
                    .setActionRow(makeButtons.apply(current))
                    .queue();
        };

        CompletableFuture<List<T>> future = new CompletableFuture<>();

        User user = discordPlayer.getMember().getUser();
        user.openPrivateChannel().queue(pv -> {
            T first = options.get(cursor[0]);
            CardEmbed firstEmbed = mapper.apply(first);
            pv.sendMessage(prompt)
                    .setFiles(FileUpload.fromData(firstEmbed.imageData(), firstEmbed.filename()))
                    .setEmbeds(firstEmbed.embed())
                    .setActionRow(makeButtons.apply(first))
                    .queue(message -> {
                        var listener = new ListenerAdapter() {
                            @Override
                            public void onButtonInteraction(ButtonInteractionEvent event) {
                                Exception ex = null;
                                try {
                                    String[] parts = event.getComponentId().split(":");
                                    if (!parts[0].equals(sessionId)) {
                                        return;
                                    }
                                    String action = parts[1];
                                    event.deferEdit().queue();
                                    switch (action) {
                                        case "previous" -> {
                                            cursor[0] = (cursor[0] - 1 + options.size()) % options.size();
                                            updateMessage.accept(message, options.get(cursor[0]));
                                        }
                                        case "next" -> {
                                            cursor[0] = (cursor[0] + 1) % options.size();
                                            updateMessage.accept(message, options.get(cursor[0]));
                                        }
                                        case "select" -> {
                                            T current = options.get(cursor[0]);
                                            if (selected.contains(current)) {
                                                selected.remove(current);
                                            } else {
                                                selected.add(current);
                                            }
                                            updateMessage.accept(message, current);
                                        }
                                        case "confirm" -> {
                                            if (!allowEmpty && selected.isEmpty()) {
                                                event.getHook().sendMessage("⚠️ Você deve selecionar pelo menos uma carta!").setEphemeral(true).queue();
                                                return;
                                            }
                                            jda.removeEventListener(this);
                                            message.delete().queue();
                                            future.complete(new ArrayList<>(selected));
                                        }
                                    }
                                } catch (Exception e) {
                                    ex = e;
                                    log.error("Exception on \"chooseMany\" event of player {}", player.getName(), e);
                                    jda.removeEventListener(this);
                                    message.delete().queue();
                                } finally {
                                    if (ex != null) {
                                        future.completeExceptionally(ex);
                                    }
                                }
                            }
                        };
                        jda.addEventListener(listener);
                    }, t -> {
                        log.error("Failed to send message to player {}", player.getName(), t);
                        future.completeExceptionally(t);
                    });
        }, t -> {
            log.error("Failed to get private channel of {}", player.getName(), t);
            future.completeExceptionally(t);
        });
        return future;
    }

    @Override
    public <T> CompletableFuture<T> chooseOne(Player player, String prompt, List<T> options, Class<T> type, boolean allowNull) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> confirm(Player player, String prompt) {
        return null;
    }

    @Override
    public <T extends Number> CompletableFuture<T> inputNumber(Player player, String prompt, Function<String, T> parser) {
        return null;
    }

    @Override
    public CompletableFuture<String> inputText(Player player, String prompt) {
        return null;
    }

    @Override
    public CompletableFuture<Void> sendMessage(Player player, String message) {
        return null;
    }

    @Override
    public CompletableFuture<Void> sendMessage(GameSession session, String message) {
        return CompletableFuture.supplyAsync(() -> {
            gameChannel.sendMessage(message).complete();
            return null;
        });
    }

    private void populateMapperCache() {
        putMapper(Card.class, embedFactory::createCardEmbed);
    }

    private <T> void putMapper(Class<T> elementType, Function<T, ?> mapper) {
        elementMapperCache.put(elementType, mapper);
    }

    @Override
    public void close() throws Exception {
        if (jda != null) {
            log.info("Shutting down JDA");
            jda.shutdown();
            int seconds = 5;
            if (!jda.awaitShutdown(Duration.ofSeconds(seconds))) {
                log.warn("JDA could not be closed in {} seconds. Shutting down forcibly.", seconds);
                jda.shutdownNow();
            }
            log.info("JDA closed.");
        }
    }

    public static boolean init() {
        var varName = "DISCORD_BOT_TOKEN";
        var token = System.getenv(varName);
        if (token == null || token.isBlank()) {
            log.error("Environment variable {} is not defined", varName);
            return false;
        }
        try {
            var jda = JDABuilder.create(token, Arrays.asList(GatewayIntent.values()))
                    .setActivity(Activity.playing("Tardis Card Game"))
                    .build();
            jda.awaitReady();
            log.info("JDA ready");

            var guild = jda.getGuildById(System.getenv("DISCORD_GUILD_ID"));
            assert guild != null; // just to shut up the compiler
            var channel = guild.getTextChannelById(System.getenv("DISCORD_CHANNEL_ID"));

            instance = new DiscordService(jda, guild, channel);
            var closer = Thread.ofVirtual().unstarted(() -> {
                try {
                    instance.close();
                } catch (Exception e) {
                    log.error("Could not close DiscordService properly", e);
                }
            });
            Runtime.getRuntime().addShutdownHook(closer);
            return true;
        } catch (InterruptedException e) {
            log.error("Interrupted while awaiting JDA to become ready", e);
            return false;
        }
    }

}
