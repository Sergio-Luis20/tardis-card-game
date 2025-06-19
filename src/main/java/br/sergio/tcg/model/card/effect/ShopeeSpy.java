package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;

public class ShopeeSpy extends EffectCard {

    public ShopeeSpy() {
        super("Espiaõ da Shopee", Rarity.COMMON, "Pode espiar uma carta aleatória de um jogador aleatório.", getImage("espião-da-shopee.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {

    }

}
