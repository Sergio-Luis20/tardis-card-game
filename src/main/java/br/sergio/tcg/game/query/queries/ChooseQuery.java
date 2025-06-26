package br.sergio.tcg.game.query.queries;

import br.sergio.tcg.Utils;
import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.query.Query;
import br.sergio.tcg.game.query.QueryManager;
import br.sergio.tcg.game.Player;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Component.Type;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public record ChooseQuery<T>(Player target, String prompt, List<T> options, boolean pv,
                             Function<T, String> mapper) implements Query<T> {

    public ChooseQuery {
        Utils.nonNull(target, prompt, options, mapper);
        int size = options.size();
        if (size < 2) {
            throw new IllegalArgumentException("Options list should have at least 2 elements, but have " + size + ".");
        }
        int max = Type.BUTTON.getMaxPerRow();
        if (size > max) {
            throw new IllegalArgumentException("Options to select must be at max " + max + ", but was " + size + ".");
        }
    }

    @Override
    public void execute(QueryManager queryManager, GameSession session) {
        String interactionId = UUID.randomUUID().toString();
        List<Button> buttons = new ArrayList<>(options.size());
        int index = 0;
        for (T option : options) {
            buttons.add(Button.primary(interactionId + ":" + index++, mapper.apply(option)));
        }
        CompletableFuture<T> selected = new CompletableFuture<>();
        Consumer<MessageChannel> channelConsumer = channel -> {
            channel.sendMessage(prompt).addActionRow(buttons).queue(message -> {
                JDA jda = DiscordService.getInstance().getJda();
                ChooseQueryListener<T> listener = ChooseQueryListener.<T>builder()
                        .jda(jda)
                        .interactionId(interactionId)
                        .player(target)
                        .future(selected)
                        .options(options)
                        .message(message)
                        .build();
                jda.addEventListener(listener);
            }, t -> {
                log.error("Failed to send message to {}", target.getName(), t);
                DiscordService.getInstance().getGameChannel().sendMessage("Não consigo te enviar " +
                        "mensagens no privado, " + target.getBoldName() + "!").queue();
                selected.completeExceptionally(t);
            });
        };
        if (pv) {
            target.getMember().getUser().openPrivateChannel().queue(channelConsumer, t -> {
                log.error("Failed to get private channel of {}", target.getName(), t);
                DiscordService.getInstance().getGameChannel().sendMessage("Não consigo te enviar " +
                        "mensagens no privado, " + target.getBoldName() + "!").queue();
                selected.completeExceptionally(t);
            });
        } else {
            channelConsumer.accept(session.getGameChannel());
        }
        selected.whenComplete((option, throwable) -> {
            if (throwable != null) {
                log.error("Query failed for {}", target.getName(), throwable);
                queryManager.completeExceptionally(this, throwable);
            } else {
                queryManager.complete(this, option);
            }
        });
    }

    @Builder
    private static class ChooseQueryListener<T> extends ListenerAdapter {

        private JDA jda;
        private String interactionId;
        private Player player;
        private CompletableFuture<T> future;
        private List<T> options;
        private Message message;

        @Override
        public void onButtonInteraction(ButtonInteractionEvent event) {
            try {
                String[] parts = event.getComponentId().split(":");
                if (parts.length != 2 || !parts[0].equals(interactionId)) {
                    return;
                }
                if (!event.getUser().equals(player.getMember().getUser())) {
                    event.reply("❌ Isso não é para você.").setEphemeral(true).queue();
                    return;
                }
                int chosenIndex = Integer.parseInt(parts[1]);
                DiscordService.getInstance().consumeInteraction(event);
                future.complete(options.get(chosenIndex));
                cleanup();
            } catch (Exception e) {
                event.reply("Erro interno").queue();
                log.error("Exception on \"choose\" event of player {}", player.getName(), e);
                cleanup();
                future.completeExceptionally(e);
            }
        }

        private void cleanup() {
            jda.removeEventListener(this);
            message.delete().queue();
        }

    }

}
