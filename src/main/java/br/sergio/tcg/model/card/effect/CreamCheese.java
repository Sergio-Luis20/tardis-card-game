package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.*;
import br.sergio.tcg.model.card.family.FoodCard;

public class CreamCheese extends EffectCard implements FoodCard, CombinableCard {

    private boolean combined;

    public CreamCheese() {
        super("Requeijão", Rarity.UNCOMMON, "Quando combinado com a carta \"Pão\", cura 50 de vida.", getImage("requeijão.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {
        if (combined) {
            player.addHp(50);
            session.log("**" + player.getName() + "** regenerou 50 de vida!");
        }
    }

    @Override
    public void whenCombined(CardCombination combination) {
        combined = combination.contains(CardRepository.getInstance().findByClass(Bread.class));
    }

}
