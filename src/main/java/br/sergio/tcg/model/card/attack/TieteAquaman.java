package br.sergio.tcg.model.card.attack;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.AttackCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;

public class TieteAquaman extends AttackCard {

    public TieteAquaman() {
        super("Aquaman do Tietê", Rarity.COMMON, "Aplica veneno que dá 4 de dano por 5 turnos.", getImage("aquaman-do-tietê.png"));
    }

    @Override
    public void onAttack(GameSession session, Player attacker, Player defender) {

    }

}
