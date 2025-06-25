package br.sergio.tcg.game.effect;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.AttributeInstance;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.Player;
import lombok.Getter;
import lombok.Setter;

@Getter
public class DoTTurnEffect implements DoTEffect {

    @Setter
    private int turns;
    private Player target;
    private AttributeInstance damage;
    private GameSession session;
    private String message;

    public DoTTurnEffect(Player target, int turns, AttributeInstance damage) {
        this(target, turns, damage, null, null);
    }

    public DoTTurnEffect(Player target, int turns, AttributeInstance damage, GameSession session, String message) {
        Utils.nonNull(target, damage);
        this.target = target;
        this.turns = turns;
        this.damage = damage;
        this.session = session;
        this.message = message;
    }

    @Override
    public AttributeInstance getAttribute() {
        return damage;
    }

    @Override
    public void tick() {
        target.subtractHp((int) damage.calculate());
        turns--;
        if (session != null && message != null) {
            session.log(message);
        }
    }

    @Override
    public boolean isExpired() {
        return turns == 0;
    }

}
