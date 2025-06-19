package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.CombinableCard;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.model.card.family.FoodCard;

public class Bread extends EffectCard implements FoodCard, CombinableCard {

    public Bread() {
        super("Pão", Rarity.COMMON, "Cura 10 de vida.", getImage("pão.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {
        player.addHp(10);
        session.log("**" + player.getName() + "** recuperou 10 de vida!");
    }

}
