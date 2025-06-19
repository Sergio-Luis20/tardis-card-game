package br.sergio.tcg.model.card.attack;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.AttackCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.model.card.family.ClownGirlCard;

public class ClownGirlHammer extends AttackCard implements ClownGirlCard {

    public ClownGirlHammer() {
        super("Martelo da garota palhaço", Rarity.RARE, "Revele da sua mão uma carta \"garota palhaço\" e logo em seguida destrua uma carta na mão do oponente.", getImage("martelo-da-garota-palhaço.png"));
    }

    @Override
    public void onAttack(GameSession session, Player attacker, Player defender) {

    }

}
