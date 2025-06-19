package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;

public class ChosenUndead extends EffectCard {

    public ChosenUndead() {
        super("Chosen Undead", Rarity.RARE, "Revive a última carta perdida. É queimada após ser usada.", getImage("chosen-undead.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {

    }

}
