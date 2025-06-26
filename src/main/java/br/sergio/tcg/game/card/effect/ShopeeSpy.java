package br.sergio.tcg.game.card.effect;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class ShopeeSpy extends EffectCard {

    public ShopeeSpy() {
        super("Espião da Shopee", Rarity.COMMON, "Pode espiar uma carta aleatória de um " +
                "jogador aleatório.", "https://i.imgur.com/qmYj1Vv.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var player = turn.getPlayer();
        var players = new ArrayList<>(turn.getSession().getPlayers());
        while (!players.isEmpty()) {
            int playerIndex = ThreadLocalRandom.current().nextInt(players.size());
            var randomPlayer = players.remove(playerIndex);
            if (randomPlayer.equals(player)) {
                continue;
            }
            var hand = randomPlayer.getHand();
            if (hand.isEmpty()) {
                continue;
            }
            int cardIndex = ThreadLocalRandom.current().nextInt(hand.size());
            var card = hand.get(cardIndex);
            turn.getSession().revealCards(randomPlayer, List.of(card), Set.of(player));
            return completedAction();
        }
        turn.log("Nenhum jogador carga alguma para espiar.");
        return completedAction();
    }

}
