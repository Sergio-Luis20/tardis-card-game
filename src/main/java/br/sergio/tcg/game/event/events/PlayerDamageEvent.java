package br.sergio.tcg.game.event.events;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.AttributeInstance;
import br.sergio.tcg.game.AttributeSource;
import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.event.Event;
import lombok.Getter;
import lombok.Setter;

/**
 * Called when a try to deal damage to a player
 * is successful, passing all possibly reductions
 * or immune systems, resulting in an amount of
 * damage that is bigger than 0.
 */
@Getter
@Setter
public class PlayerDamageEvent implements Event, AttributeSource {

    private Player damager;
    private Player target;
    private AttributeInstance damage;

    public PlayerDamageEvent(Player damager, Player target, AttributeInstance damage) {
        Utils.nonNull(damager, target, damage);
        this.damager = damager;
        this.target = target;
        this.damage = damage;
    }

    @Override
    public AttributeInstance getAttribute() {
        return damage;
    }

}
