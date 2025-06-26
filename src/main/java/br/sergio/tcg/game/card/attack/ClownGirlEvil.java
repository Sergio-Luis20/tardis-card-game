package br.sergio.tcg.game.card.attack;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.AttributeInstance;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.query.queries.SelectManyQuery;
import br.sergio.tcg.game.card.AttackCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.card.family.ClownGirlCard;

import java.util.concurrent.CompletableFuture;

public class ClownGirlEvil extends AttackCard implements ClownGirlCard {

    public ClownGirlEvil() {
        super("Garota palhaço Evil", Rarity.UNCOMMON, "Escolha quaisquer cartas da sua " +
                "mão e revele ao oponente. Para cada carta revelada, cause 10 pontos de dano à vida " +
                "dele.", "https://i.imgur.com/ZKUE9gN.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var attacker = turn.getAttacker();
        var defender = turn.getDefender();
        turn.getBattleDetails().addBattleTask(() -> {
            var query = new SelectManyQuery<>(
                    attacker,
                    "Escolha quaisquer cartas da sua mão para revelar",
                    attacker.getHand(),
                    true,
                    true,
                    card -> DiscordService.getInstance().getEmbedFactory().createCardEmbed(attacker, card)
            );
            var future = new CompletableFuture<Void>();
            turn.getSession().query(query, (q, result) -> {
                int revealed = result.size();
                if (revealed == 0) {
                    turn.logf("%s se recusou a causar dano a %s", attacker.getName(), defender.getName());
                    future.complete(null);
                    return;
                }
                var damage = new AttributeInstance(-10 * revealed);
                turn.getBattleDetails().addHpVariation(defender, damage);
                turn.logf("%s revelou %d cartas para %s, causando %d de dano no processo!", attacker.getName(), revealed, defender.getName(), damage);
                future.complete(null);
            });
            return future;
        });
        return completedAction();
    }

}
