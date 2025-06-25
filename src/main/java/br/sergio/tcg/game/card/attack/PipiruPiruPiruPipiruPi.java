package br.sergio.tcg.game.card.attack;

import br.sergio.tcg.game.IncreaseAbsOnlyAttributeInstance;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.AttackCard;
import br.sergio.tcg.game.card.Rarity;

import java.util.concurrent.CompletableFuture;

public class PipiruPiruPiruPipiruPi extends AttackCard {

    public PipiruPiruPiruPipiruPi() {
        super("Pipiru piru piru pipiru pi", Rarity.EPIC, "Inflige 60 de dano bruto ao " +
                "adversário, ignorando qualquer defesa.", getImage("pipiru-piru-piru-pipiru-pi.png"));
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var damage = new IncreaseAbsOnlyAttributeInstance(-60);
        turn.getBattleDetails().addHpVariation(turn.getDefender(), damage);
        turn.logf("%s atacou %s usando o Excaliborg!", turn.getAttacker().getBoldName(), turn.getDefender().getBoldName());
        return completedAction();
    }

}
