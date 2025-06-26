package br.sergio.tcg.game.card.effect;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.*;
import br.sergio.tcg.game.card.family.FoodCard;

import java.util.concurrent.CompletableFuture;

public class CreamCheese extends EffectCard implements FoodCard, CombinableCard {

    private boolean combined;

    public CreamCheese() {
        super("Requeijão", Rarity.UNCOMMON, "Quando combinado com a carta \"Pão\", " +
                "cura 50 de vida.", "https://i.imgur.com/etar3s9.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        if (!combined) {
            turn.logf("%s não foi combinado com %s, portanto nenhum efeito foi ativado.", getName(), bread().getName());
        } else {
            var player = turn.getPlayer();
            player.addHp(50);
            turn.logf("%s regenerou %d de vida!", player.getBoldName(), 50);
        }
        return completedAction();
    }

    @Override
    public void whenCombined(CardCombination combination) {
        combined = combination.contains(bread());
    }

    private Bread bread() {
        return CardRepository.getInstance().findByClass(Bread.class);
    }

}
