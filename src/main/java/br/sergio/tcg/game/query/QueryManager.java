package br.sergio.tcg.game.query;

import br.sergio.tcg.game.GameSession;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class QueryManager {

    @NonNull
    private GameSession<?> session;

    private Map<Query<?>, QueryCallback<? extends Query<?>, ?>> callbacks = Collections.synchronizedMap(new IdentityHashMap<>());

    public <Q extends Query<R>, R> void query(Q query, QueryCallback<Q, R> callback) {
        callbacks.put(query, callback);
        query.execute(this, session);
    }

    @SuppressWarnings("unchecked")
    public <T> void complete(Query<T> query, T result) {
        var callback = callbacks.remove(query);
        if (callback != null) {
            ((QueryCallback<? super Query<T>, T>) callback).onResult(query, result);
        } else {
            log.warn("No callback found for query: {}", query);
        }
    }

}
