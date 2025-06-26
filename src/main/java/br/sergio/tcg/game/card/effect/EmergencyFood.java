package br.sergio.tcg.game.card.effect;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.card.family.FoodCard;

import java.util.concurrent.CompletableFuture;

public class EmergencyFood extends EffectCard implements FoodCard {

    public EmergencyFood() {
        super("Comida de emergência", Rarity.RARE, "Cura 35 de vida ao ser consumida.",
                "https://i.imgur.com/3Su7Fay.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var target = turn.getPlayer();
        target.addHp(35);
        turn.logf("%s recuperou 35 de vida!", target.getBoldName());
        return completedAction();
    }

}
