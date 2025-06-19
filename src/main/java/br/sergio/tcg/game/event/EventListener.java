package br.sergio.tcg.game.event;

@FunctionalInterface
public interface EventListener<T extends Event> {

    void onEvent(Registration<T> registration, T event);

}
