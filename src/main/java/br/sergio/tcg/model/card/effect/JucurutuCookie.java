package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.model.card.family.FoodCard;

public class JucurutuCookie extends EffectCard implements FoodCard {

    public JucurutuCookie() {
        super("Bolacha Jucurutu", Rarity.UNCOMMON, "Cura 10 de vida por 3 turnos.", getImage("bolacha-jucuruty.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {

    }

}
