package br.sergio.tcg.game.card.attack;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.AttackCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.effect.TurnSkipEffect;

import java.util.concurrent.CompletableFuture;

public class AfricanJimmyNeutron extends AttackCard {

    public AfricanJimmyNeutron() {
        super("Jimmy Neutron africano", Rarity.RARE, "Para o tempo, impedindo assim o oponente de jogar por 2 turnos.", getImage("jimmy-neutron-africano.png"));
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var target = turn.getDefender();
        var effect = new TurnSkipEffect(turn.getSession(), target, 2);
        turn.getBattleDetails().addEffect(target, effect);
        turn.logf("Jimmy Neutron africano fez Za Warudo em %s, fazendo ele ficar preso no tempo por 2 turnos!", target.getBoldName());
        return completedAction();
    }

}
