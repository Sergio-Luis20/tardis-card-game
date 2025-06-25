package br.sergio.tcg.game.card.attack;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.AttributeInstance;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.AttackCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.card.family.ClownGirlCard;
import br.sergio.tcg.game.query.queries.SelectOneQuery;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class ClownGirlPsyclown extends AttackCard implements ClownGirlCard {

    public ClownGirlPsyclown() {
        super("Garota palhaço psicolhaça", Rarity.UNCOMMON, "Jogue uma moeda, escolha " +
                "um jogador e ele perderá 25 pontos de vida.", getImage("garota-palhaço-psicolhaça.png"));
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var attacker = turn.getAttacker();
        var session = turn.getSession();
        turn.getBattleDetails().addBattleTask(() -> {
            sendCoinGif(attacker, session);
            var query = new SelectOneQuery<>(
                    attacker,
                    "Escolha um jogador",
                    session.getPlayers(),
                    false,
                    true,
                    DiscordService.getInstance().getEmbedFactory()::createPlayerEmbed
            );
            var future = new CompletableFuture<Void>();
            session.query(query, (q, selected) -> {
                if (selected == null) {
                    turn.logf("%s não escolheu ninguém!", attacker.getBoldName());
                } else {
                    var damage = new AttributeInstance(-25);
                    turn.getBattleDetails().addHpVariation(selected, damage);
                    turn.logf("%s escolheu atacar %s!", attacker.getBoldName(), selected.getBoldName());
                }
                future.complete(null);
            });
            return future;
        });
        return completedAction();
    }

}
