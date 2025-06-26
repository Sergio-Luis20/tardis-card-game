package br.sergio.tcg.game.card.effect;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.card.family.ClownGirlCard;
import br.sergio.tcg.game.query.queries.SelectOneQuery;

import java.util.concurrent.CompletableFuture;

public class ClownGirlDeathsmiley extends EffectCard implements ClownGirlCard {

    public ClownGirlDeathsmiley() {
        super("Garota palhaço morterrisonha", Rarity.RARE, "Revive uma carta \"garota " +
                "palhaço\".", "https://i.imgur.com/DuMGt4B.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var target = turn.getPlayer();
        var theVoid = target.getTheVoid();
        var clownGirlCards = theVoid.stream()
                .filter(ClownGirlCard.class::isInstance)
                .toList();
        if (clownGirlCards.isEmpty()) {
            turn.log("Nenhuma carta de garota palhaço encontrada para ser revivida");
            return completedAction();
        }
        var future = new CompletableFuture<Void>();
        var query = new SelectOneQuery<>(
                target,
                "Escolha uma carta \"garota palhaço\" para reviver.",
                clownGirlCards,
                true,
                true,
                card -> DiscordService.getInstance().getEmbedFactory().createCardEmbed(target, card)
        );
        turn.getSession().query(query, (q, card) -> {
            if (card == null) {
                turn.logf("%s não escolheu nenhuma carta \"garota palhaço\" para reviver.");
            } else {
                theVoid.remove(card);
                target.getHand().add(card);
                turn.logf("%s reviveu uma carta \"garota palhaço\"!");
            }
            future.complete(null);
        });
        return future;
    }

}
