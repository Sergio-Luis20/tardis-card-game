package br.sergio.tcg.game.query;

import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface QueryCallback<Q extends Query<R>, R> {

    void onResult(Q query, R result);

    default void onError(Q query, Throwable throwable) {
        LoggerFactory.getLogger(getClass()).error("Query failed", throwable);
    }

}
