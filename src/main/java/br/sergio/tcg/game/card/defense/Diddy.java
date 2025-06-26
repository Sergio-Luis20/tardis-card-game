package br.sergio.tcg.game.card.defense;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.*;
import br.sergio.tcg.game.card.effect.Bread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class Diddy extends DefenseCard implements CombinableCard {

    private boolean combined;

    public Diddy() {
        super("Diddy", Rarity.RARE, "Quando combinada com a carta \"Pão\", uma carta " +
                "inimiga morre e vai para o céu.", "https://i.imgur.com/4xxeDN4.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        turn.resetBattle(true);
        if (!combined) {
            turn.logf("%s não foi combinada com %s, portanto nenhum efeito foi ativado.", getName(), bread().getName());
            return completedAction();
        }
        var attacker = turn.getAttacker();
        var hand = attacker.getHand();
        if (hand.isEmpty()) {
            turn.logf("%s não tem nenhuma carta para morrer e ir pro céu.");
            return completedAction();
        }
        attacker.sendCardToTheVoid(hand.get(ThreadLocalRandom.current().nextInt(hand.size())));
        turn.logf("Uma carta de %s morreu e foi pro céu.", attacker.getBoldName());
        return completedAction();
    }

    @Override
    public void whenCombined(CardCombination combination) {
        combined = combination.contains(bread());
    }

    private Bread bread() {
        return CardRepository.getInstance().findByClass(Bread.class);
    }

}
