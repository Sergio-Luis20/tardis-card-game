package br.sergio.tcg.game.card;

import br.sergio.tcg.game.TurnDetails;

import java.util.concurrent.CompletableFuture;

public class DefaultDefenseCard extends DefenseCard {

    public static final DefaultDefenseCard INSTANCE = new DefaultDefenseCard();

    private DefaultDefenseCard() {
        super("Carta de defesa padrão", Rarity.COMMON, "Carta de defesa usada " +
                "internamente quando um jogador escolhe não usar nenhuma.",
                getImage("carta-de-defesa-padrão.png"));
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        turn.getBattleDetails().clearAll();
        return completedAction();
    }

}
