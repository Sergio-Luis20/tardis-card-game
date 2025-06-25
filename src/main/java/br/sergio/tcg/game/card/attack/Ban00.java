package br.sergio.tcg.game.card.attack;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.AttackCard;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Ban00 extends AttackCard {

    public Ban00() {
        super("Ban 00", Rarity.EPIC, "Bane X cartas de efeito da mão adversária.", getImage("ban00.png"));
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        turn.getBattleDetails().addBattleTask(() -> {
            var attacker = turn.getAttacker();
            var defender = turn.getDefender();
            var effectCardsInDefenderHand = defender.getHand().stream()
                    .filter(EffectCard.class::isInstance)
                    .collect(Collectors.toCollection(ArrayList::new));
            if (effectCardsInDefenderHand.isEmpty()) {
                turn.logf("%s não tem cartas de efeito para serem banidas!", defender.getBoldName());
                return completedAction();
            }
            Collections.shuffle(effectCardsInDefenderHand);
            int cardsToBan = ThreadLocalRandom.current().nextInt(effectCardsInDefenderHand.size()) + 1;
            effectCardsInDefenderHand.subList(0, cardsToBan).forEach(defender::sendCardToTheVoid);
            turn.logf("%s baniu %d cartas de efeito da mão de %s", attacker.getBoldName(), cardsToBan, defender.getBoldName());
            return completedAction();
        });
        return completedAction();
    }

}
