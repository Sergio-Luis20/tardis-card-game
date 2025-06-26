package br.sergio.tcg.game.card;

import br.sergio.tcg.game.TurnDetails;

import java.util.concurrent.CompletableFuture;

public class DefaultDefenseCard extends DefenseCard {

    public static final DefaultDefenseCard INSTANCE = new DefaultDefenseCard();

    private DefaultDefenseCard() {
        super("Carta de defesa padrão", Rarity.COMMON, "Carta de defesa usada " +
                "internamente quando um jogador escolhe não usar nenhuma. O efeito desta carta é " +
                        "simplesmente anular qualquer coisa feita pela carta de ataque.",
                "https://i.imgur.com/pXhnEgx.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        turn.resetBattle(true);
        return completedAction();
    }

}
