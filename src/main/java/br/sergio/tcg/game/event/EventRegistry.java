package br.sergio.tcg.game.event;

import br.sergio.tcg.game.GameSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventRegistry {

    private GameSession session;
    private Map<Class<?>, List<EventListener<?>>> listeners;

    public EventRegistry(GameSession session) {
        this.session = Objects.requireNonNull(session, "session");
        listeners = new ConcurrentHashMap<>();
    }

    public <T extends Event> Registration<T> register(Class<T> eventClass, EventListener<T> listener) {
        var listeners = get(eventClass);
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        return new Registration<>(session, eventClass, listener);
    }

    public <T extends Event> List<EventListener<T>> getListeners(Class<T> eventClass) {
        return Collections.unmodifiableList(get(eventClass));
    }

    public Map<Class<?>, List<EventListener<?>>> getListeners() {
        Map<Class<?>, List<EventListener<?>>> copy = new HashMap<>(listeners.size());
        for (var entry : listeners.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    public void unregister(Registration<?> registration) {
        get(registration.eventClass()).remove(registration.listener());
    }

    public <T extends Event> void unregister(Class<T> eventClass, EventListener<T> listener) {
        get(eventClass).remove(listener);
    }

    public void unregisterAll() {
        listeners.clear();
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void callEvent(T event) {
        var eventClass = (Class<T>) event.getClass();
        for (var listener : get(eventClass)) {
            listener.onEvent(new Registration<>(session, eventClass, listener), event);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> List<EventListener<T>> get(Class<T> eventClass) {
        var list = listeners.computeIfAbsent(eventClass, c -> new ArrayList<>());
        return (List<EventListener<T>>) (List<?>) list; // doing this trick to shut up the compiler
    }

}
