package br.sergio.tcg.discord;

import br.sergio.tcg.discord.slash.SlashCommand;
import br.sergio.tcg.game.SessionManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class DiscordService implements AutoCloseable {

    public static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9áéíóúýàèìòùäëïöüâêîôûãõñç-]{1,32}");

    @Getter
    private static DiscordService instance;

    private JDA jda;
    private Guild guild;
    private TextChannel gameChannel;

    private SessionManager sessionManager;
    private EmbedFactory embedFactory;

    private DiscordService(JDA jda, Guild guild, TextChannel gameChannel) {
        this.jda = jda;
        this.guild = guild;
        this.gameChannel = gameChannel;

        sessionManager = new SessionManager();
        embedFactory = new EmbedFactory();
    }

    public void registerListener(EventListener listener) {
        jda.addEventListener(listener);
    }

    public void registerSlashCommands(Set<SlashCommand> commands, boolean force) {
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
