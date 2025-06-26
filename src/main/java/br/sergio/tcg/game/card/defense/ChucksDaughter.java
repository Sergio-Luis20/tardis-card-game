package br.sergio.tcg.game.card.defense;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.DefenseCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.effect.StatusEffect;
import br.sergio.tcg.game.event.EventListener;
import br.sergio.tcg.game.event.Registration;
import br.sergio.tcg.game.event.events.PlayerDamageEvent;

import java.util.concurrent.CompletableFuture;

public class ChucksDaughter extends DefenseCard {

    public ChucksDaughter() {
        super("Filha do Chuck", Rarity.COMMON, "Ganha para si o efeito \"vergonha " +
                "alheia\" por 2 turnos. Enquanto estiver com esse efeito, o inimigo perde no argumento, " +
                "reduzindo o dano recebido em 20%.", "https://i.imgur.com/K9QeEQ6.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var enemy = turn.getAttacker();
        var target = turn.getDefender();
        var effect = new ChucksDaughterEffect(turn.getSession(), enemy, target);
        turn.getBattleDetails().addEffect(target, effect);
        turn.logf("%s ganhou o efeito \"vergonha alheia\" por dois turnos!", target.getBoldName());
        return completedAction();
    }

    public static class ChucksDaughterEffect implements StatusEffect, EventListener<PlayerDamageEvent> {

        private static final int TURNS = 2;

        private int turns = TURNS;
        private Player damager, target;
        private GameSession session;

        public ChucksDaughterEffect(GameSession session, Player damager, Player target) {
            Utils.nonNull(session, damager, target);
            this.damager = damager;
            this.target = target;
            this.session = session;
        }

        @Override
        public void tick() {
            turns--;
        }

        @Override
        public boolean isExpired() {
            return turns <= 0;
        }

        @Override
        public void onEvent(Registration<PlayerDamageEvent> registration, PlayerDamageEvent event) {
            if (event.getDamager().equals(damager) && event.getTarget().equals(target)) {
                double reduction = -0.2;
                if (event.getAttribute().addNewMultiplier(reduction).getLastMultiplier() == reduction) {
                    session.logf("%s perdeu no argumento para %s!", damager.getBoldName(), target.getBoldName());
                } else {
                    session.logf("O efeito \"vergonha alheia\" de %s aplicado por %s falhou!", damager.getBoldName(), target.getBoldName());
                }
            }
        }

        @Override
        public void whenApplied() {
            session.listen(PlayerDamageEvent.class, this);
        }

        @Override
        public void whenRemoved() {
            session.getEventRegistry().unregister(PlayerDamageEvent.class, this);
        }

    }

}
