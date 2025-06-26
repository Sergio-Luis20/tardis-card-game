package br.sergio.tcg.game.card.attack;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.query.queries.SelectOneQuery;
import br.sergio.tcg.game.query.queries.InputNumberQuery;
import br.sergio.tcg.game.card.AttackCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.card.family.ClownGirlCard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ClownGirlHammer extends AttackCard implements ClownGirlCard {

    public ClownGirlHammer() {
        super("Martelo da garota palhaço", Rarity.RARE, "Revele da sua mão uma carta " +
                "\"garota palhaço\" e logo em seguida destrua uma carta na mão do oponente.",
                "https://i.imgur.com/hC75YoP.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var attacker = turn.getAttacker();
        var defender = turn.getDefender();
        turn.getBattleDetails().addBattleTask(() -> {
            var future = new CompletableFuture<Void>();
            int cardsInDefenderHand = defender.getHand().size();
            if (cardsInDefenderHand == 0) {
                turn.logf("%s usou a carta \"Martelo da garota palhaço\" em %s, mas %s não tinha " +
                                "nenhuma carta para ser destruída, portanto nenhum efeito foi ativado.",
                        attacker.getBoldName(), defender.getBoldName(), defender.getBoldName());
                return future;
            }
            var clownGirlCards = attacker.getHand().stream()
                    .filter(ClownGirlCard.class::isInstance)
                    .collect(Collectors.toSet());
            var query = new SelectOneQuery<>(
                    attacker,
                    "Escolha uma carta \"garota palhaço\" para revelar.",
                    new ArrayList<>(clownGirlCards),
                    true,
                    true,
                    DiscordService.getInstance().getEmbedFactory()::createCardEmbed
            );
            var session = turn.getSession();
            session.query(query, (q, card) -> {
                if (card == null) {
                    turn.logf("%s usou a carta \"Martelo da garota palhaço\", mas nenhuma " +
                                    "carta \"garota palhaço\" foi revelada, então nenhum efeito foi ativado.",
                            attacker.getBoldName());
                    future.complete(null);
                    return;
                }
                session.revealCards(attacker, List.of(card));
                if (cardsInDefenderHand == 1) {
                    defender.getTheVoid().add(defender.getHand().removeFirst());
                    turn.logf("%s usou a carta \"Martelo da garota palhaço\" em %s, destruindo assim " +
                            "sua única carta!", attacker.getBoldName(), defender.getBoldName());
                    future.complete(null);
                    return;
                }
                var cardIndexQuery = new InputNumberQuery<>(
                        attacker,
                        "Escolha um número de 1 a " + cardsInDefenderHand,
                        true,
                        str -> {
                            int n = Integer.parseInt(str);
                            if (n <= 0 || n > cardsInDefenderHand) {
                                throw new NumberFormatException("Número fora dos limites: " + n);
                            }
                            return n;
                        }
                );
                session.query(cardIndexQuery, (ciq, n) -> {
                    defender.getTheVoid().add(defender.getHand().remove(n - 1));
                    session.logf("%s usou a carta \"Martelo da garota palhaço\" em %s, destruindo " +
                            "assim sua %dª carta!", attacker.getBoldName(), defender.getBoldName(), n);
                    future.complete(null);
                });
            });
            return future;
        });
        return completedAction();
    }

}
