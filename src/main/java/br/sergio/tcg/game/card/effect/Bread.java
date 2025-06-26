package br.sergio.tcg.game.card.effect;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.CombinableCard;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.card.family.FoodCard;

import java.util.concurrent.CompletableFuture;

public class Bread extends EffectCard implements FoodCard, CombinableCard {

    public Bread() {
        super("Pão", Rarity.COMMON, "Cura 10 de vida.",
                "https://i.imgur.com/KoLRyju.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var target = turn.getPlayer();
        target.addHp(10);
        turn.logf("%s comeu um pão e regenerou %d de vida!", target.getBoldName(), 10);
        return completedAction();
    }

}
