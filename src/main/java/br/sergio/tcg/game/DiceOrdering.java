package br.sergio.tcg.game;

import br.sergio.tcg.model.Player;
import lombok.Getter;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class DiceOrdering<T extends Player> {

    private List<T> players;
    private List<T> ordered = new ArrayList<>();

    @Getter
    private Map<T, Integer> currentRolls = new HashMap<>();

    public DiceOrdering(List<T> players) {
        this.players = Objects.requireNonNull(players, "players");
    }

    public boolean submitRoll(T player, int roll) {
        if (ordered.contains(player)) {
            return false;
        }
        if (currentRolls.containsKey(player)) {
            return false;
        }
        currentRolls.put(player, roll);
        return true;
    }

    public boolean isRoundReady() {
        return players.stream()
                .filter(player -> !ordered.contains(player))
                .allMatch(currentRolls::containsKey);
    }

    public boolean resolveRound() {
        if (!isRoundReady()) {
            throw new IllegalStateException("Not everyone rolled dice on this round");
        }

        int max = currentRolls.values().stream().mapToInt(i -> i).max().orElse(-1);

        var topPlayers = currentRolls.entrySet().stream()
                .filter(entry -> entry.getValue() == max)
                .map(Entry::getKey)
                .toList();

        currentRolls.clear();

        if (topPlayers.size() == 1) {
            var remaining = getRemainingPlayers();
            var winner = topPlayers.getFirst();
            ordered.add(winner);
            if (remaining.size() == 2) {
                remaining.remove(winner);
                ordered.add(remaining.getFirst());
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean allOrdersDefined() {
        return ordered.size() == players.size();
    }

    public List<T> getOrderedList() {
        return Collections.unmodifiableList(ordered);
    }

    public List<T> getRemainingPlayers() {
        return players.stream()
                .filter(player -> !ordered.contains(player))
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
