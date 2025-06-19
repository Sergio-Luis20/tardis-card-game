package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.model.card.family.ClownGirlCard;

public class ClownGirlDeathsmiley extends EffectCard implements ClownGirlCard {

    public ClownGirlDeathsmiley() {
        super("Garota palhaço morterrisonha", Rarity.RARE, "Revive uma carta \"garota palhaço\".", getImage("garota-palhaço-morterrisonha.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {

    }

}
