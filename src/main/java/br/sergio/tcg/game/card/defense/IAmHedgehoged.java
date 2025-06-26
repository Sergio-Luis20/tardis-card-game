package br.sergio.tcg.game.card.defense;

import br.sergio.tcg.game.AttributeInstance;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.DefenseCard;
import br.sergio.tcg.game.card.Rarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class IAmHedgehoged extends DefenseCard {

    public IAmHedgehoged() {
        super("Estou ouriçado", Rarity.RARE, "Devolve dano imediato ao oponente e " +
                "ricocheteia espinhos para os demais jogadores que proferem 20% do dano original.",
                "https://i.imgur.com/g2ixv18.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var attacker = turn.getAttacker();
        var defender = turn.getDefender();
        var attackerHp = turn.getBattleDetails().getHpVariations(attacker);
        var defenderHp = turn.getBattleDetails().getHpVariations(defender);
        var hps = new ArrayList<AttributeInstance>();
        var iter = defenderHp.iterator();
        while (iter.hasNext()) {
            var hp = iter.next();
            if (hp.calculate() <= 0) {
                continue;
            }
            iter.remove();
            attackerHp.add(hp);
            hps.add(hp.clone().addNewMultiplier(-0.8));
        }
        var otherPlayers = new ArrayList<>(turn.getSession().getPlayers());
        otherPlayers.removeAll(Arrays.asList(attacker, defender));
        for (var player : otherPlayers) {
            turn.getBattleDetails().getHpVariations(player)
                    .addAll(hps.stream().map(AttributeInstance::clone).toList());
        }
        turn.logf("%s devolveu o dano a %s e jogou espinhos nos demais!", defender.getBoldName(), attacker.getBoldName());
        return completedAction();
    }

}
