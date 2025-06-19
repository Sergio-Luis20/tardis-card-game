package br.sergio.tcg.model.card.attack;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.AttackCard;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.game.event.events.PlayerStartTurnEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class AfricanJimmyNeutron extends AttackCard {

    public AfricanJimmyNeutron() {
        super("Jimmy Neutron africano", Rarity.RARE, "Para o tempo, impedindo assim o oponente de jogar por 2 turnos.", getImage("jimmy-neutron-africano.png"));
    }

    @Override
    public void onAttack(GameSession session, Player attacker, Player defender) {
        var turns = new AtomicInteger(2);
        session.listen(PlayerStartTurnEvent.class, (registration, event) -> {
            if (event.player().equals(defender)) {
                registration.session().nextTurn();
                if (turns.decrementAndGet() <= 0) {
                    registration.cancel();
                }
            }
        });
        session.log("Jimmy Neutron africano fez Za Warudo em " + defender.getName() + ", fazendo ele ficar preso no tempo por 2 turnos!");
    }

}
