package br.sergio.tcg.game.query.queries;

import br.sergio.tcg.Main;
import br.sergio.tcg.Utils;
import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.query.Query;
import br.sergio.tcg.game.query.QueryManager;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public record SelectManyQuery<T>(
        Player target,
        String prompt,
        List<T> options,
        boolean pv,
        boolean allowEmpty,
        Function<T, MessageEmbed> mapper
) implements Query<List<T>> {

    public SelectManyQuery {
        Utils.nonNull(target, prompt, options, mapper);
        if (options.isEmpty() && !allowEmpty) {
            throw new IllegalArgumentException("Can't pass an empty list of options without allowing empty result");
        }
    }

    @Override
    public void execute(QueryManager queryManager, GameSession session) {
        if (options.isEmpty()) {
            queryManager.complete(this, Collections.emptyList());
        } else if (options.size() == 1 && !allowEmpty) {
            queryManager.complete(this, List.of(options.getFirst()));
        } else {
            String interactionId = UUID.randomUUID().toString();
            AtomicInteger cursor = new AtomicInteger();
            List<T> selected = new ArrayList<>();
            Function<T, List<Button>> makeButtons = current -> {
                boolean isSelected = selected.contains(current);
                return List.of(
                        Button.primary(interactionId + ":previous", "⬅️ Anterior"),
                        Button.secondary(interactionId + ":select", isSelected ? "✅ Desmarcar" : "☑️ Selecionar"),
                        Button.primary(interactionId + ":next", "➡️ Próxima"),
                        Button.success(interactionId + ":confirm", "✔️ Confirmar")
                );
            };
            BiConsumer<Message, T> updateMessage = (message, current) -> message
                    .editMessageEmbeds(mapper.apply(current)).setActionRow(makeButtons.apply(current))
                    .queue();
            CompletableFuture<List<T>> future = new CompletableFuture<>();
            Consumer<MessageChannel> channelConsumer = channel -> {
                T first = options.get(cursor.get());
                channel.sendMessage(prompt).setEmbeds(mapper.apply(first)).setActionRow(makeButtons.apply(first)).queue(message -> {
                    JDA jda = DiscordService.getInstance().getJda();
                    SelectManyButtonInteraction<T> listener = SelectManyButtonInteraction.<T>builder()
                            .jda(jda)
                            .interactionId(interactionId)
                            .cursor(cursor)
                            .updateMessage(updateMessage)
                            .options(options)
                            .message(message)
                            .selected(selected)
                            .allowEmpty(allowEmpty)
                            .player(target)
                            .future(future)
                            .build();
                    jda.addEventListener(listener);
                }, t -> {
                    log.error("Failed to send message to {}", target.getName(), t);
                    DiscordService.getInstance().getGameChannel().sendMessage("Não consigo te enviar " +
                            "mensagens no privado, " + target.getBoldName() + "!").queue();
                    future.completeExceptionally(t);
                });
            };
            if (pv) {
                target.getMember().getUser().openPrivateChannel().queue(channelConsumer, t -> {
                    log.error("Failed to get private channel of {}", target.getName(), t);
                    DiscordService.getInstance().getGameChannel().sendMessage("Não consigo te enviar " +
                            "mensagens no privado, " + target.getBoldName() + "!").queue();
                    future.completeExceptionally(t);
                });
            } else {
                channelConsumer.accept(session.getGameChannel());
            }
            future.whenCompleteAsync((result, throwable) -> {
                if (throwable != null) {
                    log.error("Query failed for {}", target.getName(), throwable);
                    queryManager.completeExceptionally(this, throwable);
                } else {
                    queryManager.complete(this, result);
                }
            }, Main.VIRTUAL);
        }
    }

    @Builder
    private static class SelectManyButtonInteraction<T> extends ListenerAdapter {

        private JDA jda;
        private String interactionId;
        private AtomicInteger cursor;
        private BiConsumer<Message, T> updateMessage;
        private List<T> options;
        private Message message;
        private List<T> selected;
        private boolean allowEmpty;
        private Player player;
        private CompletableFuture<List<T>> future;

        @Override
        public void onButtonInteraction(ButtonInteractionEvent event) {
            Exception ex = null;
            try {
                String[] parts = event.getComponentId().split(":");
                if (parts.length != 2 || !parts[0].equals(interactionId)) {
                    log.warn("SelectManyQuery: received button event that is not for the interaction id {}", interactionId);
                    return;
                }
                if (!event.getUser().equals(player.getMember().getUser())) {
                    event.reply("❌ Isso não é para você.").setEphemeral(true).queue();
                    return;
                }
                String action = parts[1];
                event.deferEdit().queue();
                switch (action) {
                    case "previous" -> {
                        cursor.set((cursor.get() - 1 + options.size()) % options.size());
                        updateMessage.accept(message, options.get(cursor.get()));
                    }
                    case "next" -> {
                        cursor.set((cursor.get() + 1) % options.size());
                        updateMessage.accept(message, options.get(cursor.get()));
                    }
                    case "select" -> {
                        T current = options.get(cursor.get());
                        if (selected.contains(current)) {
                            selected.remove(current);
                        } else {
                            selected.add(current);
                        }
                        updateMessage.accept(message, current);
                    }
                    case "confirm" -> {
                        if (!allowEmpty && selected.isEmpty()) {
                            event.getHook().sendMessage("⚠️ Você deve selecionar pelo menos um elemento!").setEphemeral(true).queue();
                            return;
                        }
                        jda.removeEventListener(this);
                        message.delete().queue();
                        future.complete(new ArrayList<>(selected));
                    }
                }
            } catch (Exception e) {
                ex = e;
                log.error("Exception on \"selectMany\" event of player {}", player.getName(), e);
                jda.removeEventListener(this);
                message.delete().queue();
            } finally {
                if (ex != null) {
                    future.completeExceptionally(ex);
                }
            }
        }

    }

}
