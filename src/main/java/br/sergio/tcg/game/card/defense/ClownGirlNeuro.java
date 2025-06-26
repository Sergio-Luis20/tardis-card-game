package br.sergio.tcg.game.card.defense;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.AttributeInstance;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.DefenseCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.card.family.ClownGirlCard;
import br.sergio.tcg.game.query.queries.SelectManyQuery;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ClownGirlNeuro extends DefenseCard implements ClownGirlCard {

    public ClownGirlNeuro() {
        super("Garota palhaço Neuro", Rarity.UNCOMMON, "Escolha quaisquer cartas da " +
                "sua mão e revele ao oponente. Para cada carta revelada, ganhe 10 pontos de vida.",
                "https://i.imgur.com/atsVzPU.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        turn.resetBattle(true);
        var future = new CompletableFuture<Void>();
        var attacker = turn.getAttacker();
        var defender = turn.getDefender();
        var query = new SelectManyQuery<>(
                defender,
                "Escolha quaisquer cartas da sua mão para revelar a " + attacker.getName(),
                new ArrayList<>(defender.getHand()),
                true,
                true,
                card -> DiscordService.getInstance().getEmbedFactory().createCardEmbed(defender, card)
        );
        var session = turn.getSession();
        session.query(query, (q, cards) -> {
            session.revealCards(defender, cards, Set.of(attacker));
            int revealed = cards.size();
            int hp = 10 * revealed;
            var heal = new AttributeInstance(hp);
            turn.getBattleDetails().addHpVariation(defender, heal);
            turn.logf("%s revelou %d cartas para %s e regenerou %d de vida!", defender.getBoldName(),
                    revealed, attacker.getBoldName(), hp);
            future.complete(null);
        });
        return future;
    }

}
