package br.sergio.tcg.game.card.attack;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.query.queries.SelectOneQuery;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class AgakaAnus extends EffectCard {

    public AgakaAnus() {
        super("Cu do Agaka", Rarity.LEGENDARY, "Pode enviar uma carta inimiga direto " +
                "para o esquecimento.", "https://i.imgur.com/9wq8RhN.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var attacker = turn.getAttacker();
        var defender = turn.getDefender();
        turn.getBattleDetails().addBattleTask(() -> {
            var future = new CompletableFuture<Void>();
            var query = new SelectOneQuery<>(
                    attacker,
                    "Escolha uma das cartas de " + defender.getBoldName() + " para enviar para o esquecimento",
                    new ArrayList<>(defender.getHand()),
                    true,
                    true,
                    card -> DiscordService.getInstance().getEmbedFactory().createCardEmbed(defender, card)
            );
            var session = turn.getSession();
            session.query(query, (q, card) -> {
                if (card == null) {
                    turn.logf("%s não escolheu nenhuma carta de %s para enviar ao esquecimento!", attacker.getBoldName(), defender.getBoldName());
                } else {
                    defender.sendCardToTheVoid(card);
                    turn.logf("%s enviou uma carta de %s para o esquecimento!", attacker.getBoldName(), defender.getBoldName());
                }
                future.complete(null);
            });
            return future;
        });
        return completedAction();
    }

}
