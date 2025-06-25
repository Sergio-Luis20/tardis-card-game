package br.sergio.tcg.game.event.events;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.ImageEmbed;
import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.card.Card;
import br.sergio.tcg.game.event.Event;
import lombok.Getter;
import lombok.Setter;

/**
 * Event triggered when a card is shown on the chat.
 * This is not a reveal, it's the common process of
 * using a card.
 */
@Getter
@Setter
public class ShowCardEvent implements Event {

    private Player player;
    private Card card;
    private ImageEmbed cardEmbed;

    public ShowCardEvent(Player player, Card card) {
        this.player = player;
        this.card = card;
        this.cardEmbed = DiscordService.getInstance().getEmbedFactory().createCardEmbed(player, card);
    }

}
