package br.sergio.tcg.game.card.effect;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.Card;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.query.queries.ChooseQuery;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SurgicalInvestigation extends EffectCard {

    public SurgicalInvestigation() {
        super("Investigação cirúrgica", Rarity.EPIC, "À escolha do jogador, pode curar seu " +
                "campo ou investigar cartas que estão no campo adversário. Só pode optar por um desses dois" +
                " efeitos.", "https://i.imgur.com/lWZK29q.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        final var heal = "❤ Curar seu campo";
        final var spy = "☂ Investigar cartas que estão no campo inimigo";
        var player = turn.getPlayer();
        var query = new ChooseQuery<>(player, "Escolha, " + player.getBoldName() + ":", List.of(heal, spy), false, Function.identity());
        var future = new CompletableFuture<Void>();
        turn.getSession().query(query, (q, chosen) -> {
            switch (chosen) {
                case heal -> {
                    int randomHeal = ThreadLocalRandom.current().nextInt(150) + 1;
                    player.addHp(randomHeal);
                    turn.logf("%s regenerou %d de vida!", player.getBoldName(), randomHeal);
                }
                case spy -> {
                    var players = new ArrayList<>(turn.getSession().getPlayers());
                    players.remove(player);
                    List<Card> cards = players.stream()
                            .flatMap(p -> p.getHand().stream())
                            .collect(Collectors.toCollection(ArrayList::new));
                    Collections.shuffle(cards);
                    int amount = ThreadLocalRandom.current().nextInt(Math.min(Message.MAX_EMBED_COUNT, cards.size())) + 1;
                    turn.getSession().revealCards(null, cards.subList(0, amount), Set.of(player));
                    turn.logf("%s investigou cartas no campo inimigo!", player.getBoldName());
                }
                default -> {
                    var error = new AssertionError("No recognized option on choose query.");
                    future.completeExceptionally(error);
                    return;
                }
            }
            future.complete(null);
        });
        return future;
    }

}
