package br.sergio.tcg.game.query;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.Player;

import java.util.Map;

public interface Query<R> {

    Player target();

    Map<String, Object> attributes();

    @SuppressWarnings("unchecked")
    default <T> T attribute(String name) {
        return (T) attributes().get(name);
    }

    <P extends Player> void execute(QueryManager queryManager, GameSession<P> session);

}
