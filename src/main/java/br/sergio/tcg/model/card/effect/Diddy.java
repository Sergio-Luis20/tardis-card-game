package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.CardCombination;
import br.sergio.tcg.model.card.CombinableCard;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.card.Rarity;

public class Diddy extends EffectCard implements CombinableCard {

    public Diddy() {
        super("Diddy", Rarity.RARE, "Quando combinada com a carta \"Pão\", uma carta inimiga morre e vai para o céu.", getImage("diddy.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {

    }

    @Override
    public void whenCombined(CardCombination combination) {
        // TODO
    }

}
