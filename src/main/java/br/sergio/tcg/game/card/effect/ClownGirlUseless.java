package br.sergio.tcg.game.card.effect;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.card.family.ClownGirlCard;
import br.sergio.tcg.game.query.queries.SelectOneQuery;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ClownGirlUseless extends EffectCard implements ClownGirlCard {

    public ClownGirlUseless() {
        super("Garota palhaço inútil", Rarity.COMMON, "Descarte esta carta e mais outra e " +
                "depois compre uma carta.", "https://i.imgur.com/rVv1amC.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var target = turn.getPlayer();
        var session = turn.getSession();
        var query = new SelectOneQuery<>(
                target,
                "Escolha uma carta para descartar juntamente da " + getName() + ".",
                new ArrayList<>(target.getHand()),
                true,
                true,
                card -> DiscordService.getInstance().getEmbedFactory().createCardEmbed(target, card)
        );
        var future = new CompletableFuture<Void>();
        session.query(query, (q, card) -> {
            if (card == null) {
                turn.logf("%s não escolheu nenhuma carta para descartar, portanto nenhum efeito será ativado.", target.getBoldName());
            } else {
                session.sendCardToDeck(target, card);
                session.draw(target);
                turn.logf("%s descartou uma carta juntamente da %s e comprou 1 carta!", target.getName(), getName());
            }
            future.complete(null);
        });
        return future;
    }

}
