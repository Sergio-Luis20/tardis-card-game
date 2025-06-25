package br.sergio.tcg.game.query;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.Player;

public interface Query<R> {

    Player target();

    void execute(QueryManager queryManager, GameSession session);

}
