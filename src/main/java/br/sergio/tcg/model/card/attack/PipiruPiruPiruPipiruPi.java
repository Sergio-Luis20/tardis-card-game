package br.sergio.tcg.model.card.attack;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.AttackCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;

public class PipiruPiruPiruPipiruPi extends AttackCard {

    public PipiruPiruPiruPipiruPi() {
        super("Pipiru piru piru pipiru pi", Rarity.EPIC, "Inflige 60 de dano bruto ao adversário, ignorando qualquer defesa.", getImage("pipiru-piru-piru-pipiru-pi.png"));
    }

    @Override
    public void onAttack(GameSession session, Player attacker, Player defender) {

    }

}
