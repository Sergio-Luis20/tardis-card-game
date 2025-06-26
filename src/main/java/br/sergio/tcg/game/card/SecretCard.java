package br.sergio.tcg.game.card;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.card.family.CardFamily;

public interface SecretCard extends CardFamily {

    default void sendLog(GameSession session, Player player) {
        session.logf("%s jogou uma carta secreta!", player.getBoldName());
    }

    default String getFamilyName() {
        return "Carta secreta";
    }

}
