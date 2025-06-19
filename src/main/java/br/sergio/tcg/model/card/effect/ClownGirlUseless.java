package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.model.card.family.ClownGirlCard;

public class ClownGirlUseless extends EffectCard implements ClownGirlCard {

    public ClownGirlUseless() {
        super("Garota palhaço inútil", Rarity.COMMON, "Descarte esta carta e mais outra e depois compre uma carta.", getImage("garota-palhaço-inútil.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {

    }

}
