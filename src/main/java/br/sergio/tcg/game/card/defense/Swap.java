package br.sergio.tcg.game.card.defense;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.DefenseCard;
import br.sergio.tcg.game.card.Rarity;

import java.util.concurrent.CompletableFuture;

public class Swap extends DefenseCard {

    public Swap() {
        super("Trocar!", Rarity.UNCOMMON, "Troca a carta anteriormente usada pela carta " +
                "atual do adversário.", "https://i.imgur.com/SuK1kKL.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        turn.resetBattle(true);
        var attacker = turn.getAttacker();
        var defender = turn.getDefender();
        var attackCard = turn.getBattleDetails().getAttackCard();
        attacker.getHand().remove(attackCard);
        defender.getHand().add(attackCard);
        var defenderHistory = defender.getHistory();
        var lastCardName = "nenhuma";
        if (!defenderHistory.isEmpty()) {
            var lastCard = defenderHistory.removeLast();
            attacker.getHand().add(lastCard);
            lastCardName = lastCard.getName();
        }
        turn.logf(
                "%s trocou sua última carta usada (%s) pela carta atual de %s (%s)!",
                defender.getBoldName(),
                lastCardName,
                attacker.getBoldName(),
                attackCard.getName()
        );
        return completedAction();
    }

}
