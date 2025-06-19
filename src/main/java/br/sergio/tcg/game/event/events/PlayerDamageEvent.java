package br.sergio.tcg.game.event.events;

import br.sergio.tcg.Utils;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.game.event.Event;

/**
 * Called when a try to deal damage to a player
 * is successful, passing all possibly reductions
 * or immune systems, resulting in an amount of
 * damage that is bigger than 0.
 */
public record PlayerDamageEvent(Player damager, Player target, int damage) implements Event {

    public PlayerDamageEvent {
        Utils.nonNull(damager, target);
        if (damage <= 0) throw new IllegalArgumentException("damage must be positive");
    }

}
