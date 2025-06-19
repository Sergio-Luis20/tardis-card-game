package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.model.card.family.ClownGirlCard;

public class ClownGirlGeiru extends EffectCard implements ClownGirlCard {

    public ClownGirlGeiru() {
        super("Garota palhaço Geiru", Rarity.RARE, "Jogue uma moeda, escolha um jogador e o outro deverá descartar ou comprar cartas até ter o mesmo número de cartas do oponente.", getImage("garota-palhaço-geiru.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {

    }

}
