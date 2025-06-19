package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;

public class ReturnByDeath extends EffectCard {

    public ReturnByDeath() {
        super("Retorno através da morte", Rarity.RARE, "Revive a última carta aliada morta", getImage("retorno-através-da-morte.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {

    }

}
