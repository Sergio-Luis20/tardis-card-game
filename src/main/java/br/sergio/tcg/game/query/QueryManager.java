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
    private GameSession session;

    private Map<Query<?>, QueryCallback<? extends Query<?>, ?>> callbacks = Collections.synchronizedMap(new IdentityHashMap<>());

    public <Q extends Query<R>, R> void query(Q query, QueryCallback<Q, R> callback) {
        callbacks.put(query, callback);
        Thread.startVirtualThread(() -> query.execute(this, session));
    }

    @SuppressWarnings("unchecked")
    public <T> void complete(Query<T> query, T result) {
        log.info("Completing query {} for {}", query.getClass().getName(), query.target().getName());
        var callback = callbacks.remove(query);
        if (callback != null) {
            log.info("Calling query {} callback to {}", query.getClass().getName(), query.target().getName());
            ((QueryCallback<? super Query<T>, T>) callback).onResult(query, result);
        } else {
            log.warn("No callback found for query: {}", query);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void completeExceptionally(Query<T> query, Throwable throwable) {
        var callback = callbacks.remove(query);
        if (callback != null) {
            ((QueryCallback<? super Query<T>, T>) callback).onError(query, throwable);
        } else {
            log.warn("No callback found for query: {} on exceptionally completion.", query, throwable);
        }
    }

}
