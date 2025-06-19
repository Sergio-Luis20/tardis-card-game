package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;

public class MeathookBeta extends EffectCard {

    public MeathookBeta() {
        super("Meathook beta", Rarity.EPIC, "Rouba 2 cartas inimigas.", getImage("meathook-beta.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {

    }

}
