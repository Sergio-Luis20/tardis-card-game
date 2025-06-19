package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;

public class AgakaAnus extends EffectCard {

    public AgakaAnus() {
        super("Cu do Agaka", Rarity.LEGENDARY, "Pode enviar uma carta inimiga direto para o esquecimento.", getImage("cu-do-agaka.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {
        
    }

}
