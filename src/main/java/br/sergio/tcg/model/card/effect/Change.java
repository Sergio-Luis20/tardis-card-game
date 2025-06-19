package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;

public class Change extends EffectCard {

    public Change() {
        super("Trocar!", Rarity.UNCOMMON, "Troca a carta anteriormente usada pela carta atual do adversário.", getImage("trocar.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {

    }

}
