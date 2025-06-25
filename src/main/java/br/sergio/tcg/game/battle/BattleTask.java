package br.sergio.tcg.game.battle;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface BattleTask {

    CompletableFuture<Void> action();

}
