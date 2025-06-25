package br.sergio.tcg.game.effect;

public interface StatusEffect {

    void tick();

    boolean isExpired();

    default boolean isCumulative() {
        return false;
    }

    default void whenApplied() {
    }

    default void whenRemoved() {
    }

    default boolean permitsDuplicate() {
        return true;
    }

}
