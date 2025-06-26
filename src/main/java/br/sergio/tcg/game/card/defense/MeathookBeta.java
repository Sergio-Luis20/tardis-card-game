package br.sergio.tcg.game.card.defense;

import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.Card;
import br.sergio.tcg.game.card.DefenseCard;
import br.sergio.tcg.game.card.Rarity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class MeathookBeta extends DefenseCard {

    public MeathookBeta() {
        super("Meathook beta", Rarity.EPIC, "Rouba 2 cartas inimigas.",
                "https://i.imgur.com/lmJSb9Z.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        turn.resetBattle(true);
        var attacker = turn.getAttacker();
        var defender = turn.getDefender();
        var cards = removeCards(attacker, 2);
        defender.getHand().addAll(cards);
        turn.logf("%s roubou %d carta(s) de %s!", defender.getBoldName(), cards.size(), attacker.getName());
        return completedAction();
    }

    private List<Card> removeCards(Player player, int n) {
        var hand = player.getHand();
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (hand.isEmpty()) {
                break;
            }
            int index = ThreadLocalRandom.current().nextInt(hand.size());
            cards.add(hand.remove(index));
        }
        return cards;
    }

}
