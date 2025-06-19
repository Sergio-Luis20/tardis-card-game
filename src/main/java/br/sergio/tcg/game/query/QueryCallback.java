package br.sergio.tcg.game.query;

@FunctionalInterface
public interface QueryCallback<Q extends Query<R>, R> {

    void onResult(Q query, R result);

    default <T> T attribute(Query<?> query, String name) {
        return query.attribute(name);
    }

}
