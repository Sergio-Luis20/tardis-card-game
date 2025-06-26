package br.sergio.tcg.game.card.effect;

import br.sergio.tcg.Main;
import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.Card;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.card.family.ClownGirlCard;
import br.sergio.tcg.game.query.queries.SelectManyQuery;
import br.sergio.tcg.game.query.queries.SelectOneQuery;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ClownGirlGeiru extends EffectCard implements ClownGirlCard {

    public ClownGirlGeiru() {
        super("Garota palhaço Geiru", Rarity.RARE, "Jogue uma moeda, escolha um jogador " +
                "e o outro deverá descartar ou comprar cartas até ter o mesmo número de cartas do " +
                "oponente.", "https://i.imgur.com/HCPjgG5.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var player = turn.getPlayer();
        var session = turn.getSession();
        sendCoinGif(player, session);
        return CompletableFuture.supplyAsync(() -> {
            try {
                Player playerOne, playerTwo;
                do {
                    playerOne = choosePlayer(player, session, "Escolha o primeiro jogador").get();
                    playerTwo = choosePlayer(player, session, "Escolha o segundo jogador").get();
                    if (playerOne.equals(playerTwo)) {
                        turn.logf("Escolha dois jogadores distintos");
                    }
                } while (playerOne.equals(playerTwo));
                var firstPlayer = playerOne;
                var secondPlayer = playerTwo;
                turn.logf("%s deverá igualar a quantidade de cartas na sua mão à quantidade de " +
                        "cartas na mão de %s", secondPlayer.getBoldName(), firstPlayer.getBoldName());
                int amountTarget = firstPlayer.getHand().size();
                int currentAmount = secondPlayer.getHand().size();
                if (currentAmount > amountTarget) {
                    int diff = currentAmount - amountTarget;
                    turn.logf("%s deverá descartar %d cartas!", secondPlayer.getBoldName(), diff);
                    var selected = new ArrayList<Card>();
                    while (selected.size() != diff) {
                        var latch = new CountDownLatch(1);
                        var query = new SelectManyQuery<>(
                                secondPlayer,
                                "Escolha " + diff + " para descartar.",
                                new ArrayList<>(secondPlayer.getHand()),
                                true,
                                false,
                                card -> DiscordService.getInstance().getEmbedFactory().createCardEmbed(secondPlayer, card)
                        );
                        session.query(query, (q, selectedCards) -> {
                            if (selectedCards.size() != diff) {
                                turn.logf("Quantidade de cartas indevida: %d. Você deve selecionar %d cartas.", selectedCards.size(), diff);
                            } else {
                                selected.addAll(selectedCards);
                            }
                            latch.countDown();
                        });
                        latch.await();
                    }
                    for (var card : selected) {
                        session.sendCardToDeck(secondPlayer, card);
                    }
                    turn.logf("%d cartas de %s foram enviadas de volta ao deck!", diff, secondPlayer.getBoldName());
                } else if (currentAmount < amountTarget) {
                    while (secondPlayer.getHand().size() < amountTarget && !session.getDeck().isEmpty()) {
                        session.draw(secondPlayer);
                    }
                    turn.logf("%s foi forçado a comprar %d cartas.", secondPlayer.getBoldName(), amountTarget - currentAmount);
                } else {
                    turn.logf("%s já possui a mesma quantidade de cartas que %s.", secondPlayer.getBoldName(), firstPlayer.getBoldName());
                }
            } catch (Exception e) {
                log.error("Exception while performing action of card {}", getName(), e);
            }
            return null;
        }, Main.VIRTUAL);
    }

    private CompletableFuture<Player> choosePlayer(Player chooser, GameSession session, String prompt) {
        var query = new SelectOneQuery<>(
                chooser,
                prompt,
                new ArrayList<>(session.getPlayers()),
                false,
                false,
                DiscordService.getInstance().getEmbedFactory()::createPlayerEmbed
        );
        var future = new CompletableFuture<Player>();
        session.query(query, (q, player) -> future.complete(player));
        return future;
    }

}
