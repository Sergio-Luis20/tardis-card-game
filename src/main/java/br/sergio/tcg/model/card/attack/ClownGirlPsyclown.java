package br.sergio.tcg.model.card.attack;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.AttackCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.model.card.family.ClownGirlCard;

public class ClownGirlPsyclown extends AttackCard implements ClownGirlCard {

    public ClownGirlPsyclown() {
        super("Garota palhaço psicolhaça", Rarity.UNCOMMON, "Jogue uma moeda, escolha um jogador e ele perderá 25 pontos de vida.", getImage("garota-palhaço-psicolhaça.png"));
    }

    @Override
    public void onAttack(GameSession session, Player attacker, Player defender) {

    }

}
