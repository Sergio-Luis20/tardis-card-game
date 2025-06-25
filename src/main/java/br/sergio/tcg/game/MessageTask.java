package br.sergio.tcg.game;

public record MessageTask(GameSession session, String message, Object... args) {

    public void send() {
        session.logf(message, args);
    }

}
