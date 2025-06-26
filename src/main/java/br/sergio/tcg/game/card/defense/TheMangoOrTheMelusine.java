package br.sergio.tcg.game.card.defense;

import br.sergio.tcg.game.AttributeInstance;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.query.queries.ChooseQuery;
import br.sergio.tcg.game.card.DefenseCard;
import br.sergio.tcg.game.card.Rarity;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class TheMangoOrTheMelusine extends DefenseCard {

    public TheMangoOrTheMelusine() {
        super("A manga ou a melusine?", Rarity.UNCOMMON, "Ao ser ativada, o usuário pode " +
                "escolher entre \"manga\" ou \"melusine\". Se escolher manga, tem 60% de dano de reduzir " +
                "o dano tomado pela metade. Se escolher melusine, tem 40% de dano de contra-atacar o " +
                "oponente causando-lhe um dano fixo de 10 pontos.", "https://i.imgur.com/HjBOxyc.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var attacker = turn.getAttacker();
        var defender = turn.getDefender();
        final var mango = "\uD83E\uDD6D Manga";
        final var melusine = "≽^•⩊•^≼ Melusine";
        var choose = new ChooseQuery<>(
                defender,
                "A manga ou a melusine?",
                List.of(mango, melusine),
                false,
                Function.identity()
        );
        var future = new CompletableFuture<Void>();
        turn.getSession().query(choose, (q, chosen) -> {
            switch (chosen) {
                case mango -> {
                    if (Math.random() < 0.6) {
                        turn.getBattleDetails().getHpVariations(defender).stream()
                                .filter(hp -> hp.calculate() < 0)
                                .forEach(hp -> hp.addNewMultiplier(-0.5));
                        turn.logf("%s escolheu a manga e reduziu o dano pela metade!", defender.getBoldName());
                    } else {
                        turn.logf("%s escolheu a manga, mas falhou ao tentar reduzir o dano pela metade!", defender.getBoldName());
                    }
                }
                case melusine -> {
                    if (Math.random() < 0.4) {
                        turn.getBattleDetails().addHpVariation(attacker, new AttributeInstance(-10));
                        turn.logf("%s escolheu a melusine e contra-atacou %s, proferindo-lhe 10 pontos de dano!", defender.getBoldName(), attacker.getBoldName());
                    } else {
                        turn.logf("%s escolheu a melusine, mas falhou ao tentar contra-atacar %s!", defender.getBoldName(), attacker.getBoldName());
                    }
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
