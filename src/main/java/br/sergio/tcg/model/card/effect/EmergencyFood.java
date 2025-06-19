package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.model.card.family.FoodCard;

public class EmergencyFood extends EffectCard implements FoodCard {

    public EmergencyFood() {
        super("Comida de emergência", Rarity.RARE, "Cura 35 de vida ao ser consumida.", getImage("comida-de-emergência.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {
        player.addHp(35);
        session.log("**" + player.getName() + "** recuperou 35 de vida!");
    }

}
