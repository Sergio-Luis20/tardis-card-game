package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;

public class SurgicalInvestigation extends EffectCard {

    public SurgicalInvestigation() {
        super("Investigação cirúrgica", Rarity.EPIC, "À escolha do jogador, pode curar seu campo ou investigar cartas que estão no campo adversário. Só pode optar por um desses dois efeitos.", getImage("investigação-cirúrgica.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {

    }

}
