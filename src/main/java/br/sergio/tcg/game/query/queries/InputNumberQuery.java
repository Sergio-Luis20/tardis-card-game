package br.sergio.tcg.game.query.queries;

import br.sergio.tcg.Main;
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
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public record InputNumberQuery<T extends Number>(Player target, String prompt, boolean pv,
                                                 Function<String, T> parser) implements Query<T> {

    public InputNumberQuery {
        Utils.nonNull(target, prompt, parser);
    }

    @Override
    public void execute(QueryManager queryManager, GameSession session) {
        String interactionId = UUID.randomUUID().toString();
        CompletableFuture<T> future = new CompletableFuture<>();
        Button button = Button.primary(interactionId + ":input", "➕ Inserir número");
        Consumer<MessageChannel> channelConsumer = channel -> channel
                .sendMessage(prompt).setActionRow(button).queue(message -> {
                    JDA jda = DiscordService.getInstance().getJda();
                    InputNumberListener<T> listener = InputNumberListener.<T>builder()
                            .jda(jda)
                            .message(message)
                            .interactionId(interactionId)
                            .future(future)
                            .player(target)
                            .parser(parser)
                            .build();
                    jda.addEventListener(listener);
                }, t -> {
                    log.error("Failed to send message to {}", target.getName(), t);
                    DiscordService.getInstance().getGameChannel().sendMessage("Não consigo te enviar " +
                            "mensagens no privado, " + target.getBoldName() + "!").queue();
                    future.completeExceptionally(t);
                });
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

    @Builder
    private static class InputNumberListener<T> extends ListenerAdapter {

        private JDA jda;
        private Message message;
        private String interactionId;
        private CompletableFuture<T> future;
        private Player player;
        private Function<String, T> parser;

        @Override
        public void onButtonInteraction(ButtonInteractionEvent event) {
            try {
                if (!event.getComponentId().equals(interactionId + ":input")) {
                    log.warn("InputNumberQuery: received button event that is not for the interaction id {}", interactionId);
                    return;
                }
                if (!event.getUser().equals(player.getMember().getUser())) {
                    event.reply("❌ Isso não é para você.").setEphemeral(true).queue();
                    return;
                }
                TextInput input = TextInput.create("input-number", "Número", TextInputStyle.SHORT)
                        .setPlaceholder("Digite um número...")
                        .setRequired(true)
                        .build();
                Modal modal = Modal.create(interactionId + ":modal", "Inserir número")
                        .addActionRow(input)
                        .build();
                event.replyModal(modal).queue();
            } catch (Exception e) {
                event.reply("Erro interno").setEphemeral(true).queue();
                completeWithException(e);
            }
        }

        @Override
        public void onModalInteraction(ModalInteractionEvent event) {
            try {
                if (!event.getModalId().equals(interactionId + ":modal")) {
                    return;
                }
                if (!event.getUser().equals(player.getMember().getUser())) {
                    event.reply("❌ Isso não é para você.").setEphemeral(true).queue();
                    return;
                }
                String inputValue = event.getValue("input-number").getAsString();
                event.deferReply(true).queue();
                InteractionHook hook = event.getHook();
                try {
                    T parsed = parser.apply(inputValue);
                    hook.sendMessage("✅ Número inserido: **" + parsed + "**").queue();
                    cleanup();
                    future.complete(parsed);
                } catch (NumberFormatException e) {
                    hook.sendMessage("❌ **Entrada inválida:** " + e.getMessage() + "\nTente novamente clicando no botão.").queue();
                }
            } catch (Exception e) {
                completeWithException(e);
            }
        }

        private void cleanup() {
            jda.removeEventListener(this);
            message.delete().queue();
        }

        private void completeWithException(Exception e) {
            log.error("Exception on InputNumberQuery for player {}", player.getName(), e);
            cleanup();
            future.completeExceptionally(e);
        }

    }

}
