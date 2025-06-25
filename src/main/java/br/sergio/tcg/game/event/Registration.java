package br.sergio.tcg.game.event;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.GameSession;

public record Registration<T extends Event>(GameSession session, Class<T> eventClass, EventListener<T> listener) {

    public Registration {
        Utils.nonNull(session, eventClass, listener);
    }

    public void cancel() {
        session.getEventRegistry().unregister(this);
    }

}
