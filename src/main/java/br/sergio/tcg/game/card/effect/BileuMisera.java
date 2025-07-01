package br.sergio.tcg.game.card.effect;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.AttributeInstance;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.effect.DefensiveEffect;
import br.sergio.tcg.game.effect.OffensiveEffect;
import br.sergio.tcg.game.event.EventListener;
import br.sergio.tcg.game.event.Registration;
import br.sergio.tcg.game.event.events.PlayerDamageEvent;

import java.util.concurrent.CompletableFuture;

public class BileuMisera extends EffectCard {

    public BileuMisera() {
        super("BileuMisera", Rarity.COMMON, "Cura 5 de vida por 5 turnos. Se você sofrer " +
                "algum dano, ganha o efeito \"hipertensão\", que faz perder 2 de vida pelos turnos " +
                "restantes.", "https://i.imgur.com/JNE2k7u.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var target = turn.getPlayer();
        target.addEffect(new BileuMiseraEffect(turn.getSession(), target));
        turn.logf("%s ganhou o efeito \"BileuMisera\"!", target.getBoldName());
        return completedAction();
    }

    public static class BileuMiseraEffect implements OffensiveEffect, DefensiveEffect, EventListener<PlayerDamageEvent> {

        private boolean damaged;
        private AttributeInstance heal, damage;
        private int turns;
        private Player target;
        private GameSession session;

        public BileuMiseraEffect(GameSession session, Player target) {
            Utils.nonNull(session, target);
            this.target = target;
            this.session = session;
            this.heal = new AttributeInstance(5);
            this.damage = new AttributeInstance(-5);
            this.turns = 5;
        }

        @Override
        public AttributeInstance getAttribute() {
            return damaged ? damage: heal;
        }

        @Override
        public void tick() {
            if (damaged) {
                int damage = (int) this.damage.calculate();
                target.addHp(damage);
                session.logf("%s tomou %d de dano por hipertensão!", target.getBoldName(), damage);
            } else {
                int heal = (int) this.heal.calculate();
                target.addHp(heal);
                session.logf("%s regenerou 5 de vida por BileuMisera!", target.getBoldName());
            }
            turns--;
        }

        @Override
        public boolean isExpired() {
            return turns <= 0;
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
            if (!event.getTarget().equals(target) || damaged) {
                return;
            }
            damaged = true;
            session.logf("%s recebeu dano e ganhou o efeito hipertensão por BileuMisera!", target.getBoldName());
        }

    }

}
