package br.sergio.tcg.game.event.events;

import br.sergio.tcg.Utils;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.game.event.Event;

public record PlayerStartTurnEvent(Player player) implements Event {

    public PlayerStartTurnEvent {
        Utils.nonNull(player);
    }

}
