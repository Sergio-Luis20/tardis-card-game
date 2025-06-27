package br.sergio.tcg.game;

import lombok.Getter;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DiceOrdering {

    private List<Player> players;
    private List<Player> ordered = Collections.synchronizedList(new ArrayList<>());

    @Getter
    private Map<Player, Integer> currentRolls = new ConcurrentHashMap<>();

    public DiceOrdering(List<Player> players) {
        this.players = Collections.synchronizedList(Objects.requireNonNull(players, "players"));
    }

    public boolean submitRoll(Player player, int roll) {
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
        if (allOrdersDefined()) {
            return true;
        }
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

    public List<Player> getOrderedList() {
        return ordered;
    }

    public List<Player> getRemainingPlayers() {
        return players.stream()
                .filter(player -> !ordered.contains(player))
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
