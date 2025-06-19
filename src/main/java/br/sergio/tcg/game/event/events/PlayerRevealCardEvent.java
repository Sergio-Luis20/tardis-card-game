package br.sergio.tcg.game.event.events;

import br.sergio.tcg.Utils;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Card;
import br.sergio.tcg.game.event.Event;

import java.util.List;
import java.util.Set;

public record PlayerRevealCardEvent(Player player, List<Card> revealedCards, Set<Player> whoCanSee) implements Event {

    public PlayerRevealCardEvent {
        Utils.nonNull(player, revealedCards, whoCanSee);
    }

}
