package br.sergio.tcg.game.card.effect;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;

import java.util.concurrent.CompletableFuture;

public class ReturnByDeath extends EffectCard {

    public ReturnByDeath() {
        super("Retorno através da morte", Rarity.RARE, "Revive a última carta aliada morta", getImage("retorno-através-da-morte.png"));
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var player = turn.getPlayer();
        var theVoid = player.getTheVoid();
        if (theVoid.isEmpty()) {
            turn.log("Nenhuma carta morta para reviver.");
            return completedAction();
        }
        player.getHand().add(theVoid.removeLast());
        turn.logf("%s reviveu sua última carta morta!", player.getBoldName());
        return completedAction();
    }

}
