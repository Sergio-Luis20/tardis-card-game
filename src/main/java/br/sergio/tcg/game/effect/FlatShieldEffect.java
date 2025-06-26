package br.sergio.tcg.game.effect;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.AttributeInstance;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.event.EventListener;
import br.sergio.tcg.game.event.Registration;
import br.sergio.tcg.game.event.events.PlayerDamageEvent;

public class FlatShieldEffect implements DefensiveEffect, EventListener<PlayerDamageEvent> {

    private AttributeInstance shield;
    private Player target;
    private GameSession session;

    public FlatShieldEffect(Player target, GameSession session, int shieldStrength) {
        Utils.nonNull(target, session);
        this.target = target;
        this.session = session;
        this.shield = new AttributeInstance(shieldStrength);
    }

    @Override
    public AttributeInstance getAttribute() {
        return shield;
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean isExpired() {
        return shield.calculate() <= 0;
    }

    @Override
    public void whenApplied() {
        session.listen(PlayerDamageEvent.class, this);
    }

    @Override
    public void whenRemoved() {
        session.getEventRegistry().unregister(PlayerDamageEvent.class, this);
    }

    @Override
    public void onEvent(Registration<PlayerDamageEvent> registration, PlayerDamageEvent event) {
        if (!event.getTarget().equals(target)) {
            return;
        }

        var damage = event.getDamage();
        double incoming = damage.calculate();
        double absorbable = Math.min(incoming, shield.calculate());

        shield.subtractFlat(absorbable);
        damage.subtractFlat(absorbable);

        session.logf(
                "%s reduziu o dano em %d. Durabilidade restante do escudo: %d.",
                target.getBoldName(),
                (int) absorbable,
                (int) shield.calculate()
        );

        if (isExpired()) {
            session.logf("O escudo de %s quebrou!", target.getBoldName());
            target.removeEffect(this);
        }
    }

}
