package br.sergio.tcg.game.card.effect;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;

import java.util.concurrent.CompletableFuture;

public class ChosenUndead extends EffectCard {

    public ChosenUndead() {
        super("Chosen Undead", Rarity.RARE, "Revive a última carta perdida. É queimada após " +
                "ser usada.", "https://i.imgur.com/3Px3MGU.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var target = turn.getPlayer();
        var theVoid = target.getTheVoid();
        if (theVoid.isEmpty()) {
            turn.logf("%s não tinha nenhuma carta para reviver.", target.getBoldName());
        } else {
            var resurrectedCard = theVoid.removeLast();
            target.getHand().add(resurrectedCard);
            turn.logf("%s reviveu sua última carta perdida: %s!", target.getBoldName(), resurrectedCard.getName());
        }
        target.sendCardToTheVoid(this);
        turn.logf("A carta %s foi queimada!", getName());
        return completedAction();
    }

}
