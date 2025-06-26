package br.sergio.tcg.discord;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.card.Card;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@Slf4j
public class CardScrollListener extends ListenerAdapter {

    private String interactionId;
    private Player player;
    private Message cardsMessage;
    private JDA jda;
    private List<Card> cards;
    private Map<Player, CardScrollListener> map;
    private int cursor;

    public CardScrollListener(String interactionId, Player player, Message cardsMessage, JDA jda,
                              List<Card> cards, Map<Player, CardScrollListener> map) {
        Utils.nonNull(interactionId, player, cardsMessage, jda, cards, map);

        this.interactionId = interactionId;
        this.player = player;
        this.cardsMessage = cardsMessage;
        this.jda = jda;
        this.cards = cards;
        this.map = map;

        jda.addEventListener(this);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        try {
            String[] parts = event.getComponentId().split(":");
            if (parts.length != 2 || !parts[0].equals(interactionId)) {
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
                    cursor = (cursor - 1 + cards.size()) % cards.size();
                    updateMessage();
                }
                case "next" -> {
                    cursor = (cursor + 1) % cards.size();
                    updateMessage();
                }
                case "delete" -> {
                    cleanup();
                    map.remove(player);
                }
                default -> {
                    cleanup();
                    throw new AssertionError("Option not in presets: " + action);
                }
            };
        } catch (Exception e) {
            log.error("Exception during cards listener execution", e);
            event.reply("Erro interno.").queue();
        }
    }

    private void updateMessage() {
        cardsMessage.editMessageEmbeds(cardEmbed(cards.get(cursor)))
                .setActionRow(makeButtons(interactionId, cards))
                .queue(null, t -> log.error("Failed to update cards message", t));
    }

    private MessageEmbed cardEmbed(Card card) {
        return DiscordService.getInstance().getEmbedFactory().createCardEmbed(player, card);
    }

    public static List<Button> makeButtons(String interactionId, List<Card> cards) {
        var previous = Button.primary(interactionId + ":previous", "⬅️ Anterior").withDisabled(cards.size() == 1);
        var next = Button.primary(interactionId + ":next", "➡️ Próxima").withDisabled(cards.size() == 1);
        var delete = Button.danger(interactionId + ":delete", "❌ Apagar mensagem");
        return List.of(previous, next, delete);
    }

    public void cleanup() {
        jda.removeEventListener(this);
        cardsMessage.delete().queue(null, t -> log.error("Failed to delete cards message", t));
    }

}
