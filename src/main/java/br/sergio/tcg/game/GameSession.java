package br.sergio.tcg.game;

import br.sergio.tcg.Utils;
import br.sergio.tcg.model.AttackCard;
import br.sergio.tcg.model.Card;
import br.sergio.tcg.model.DefenseCard;
import br.sergio.tcg.model.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class GameSession {

    public static final int MIN_PLAYERS = 4, MAX_PLAYERS = 8;

    private List<Player> players;
    private int orderCursor;

    @Getter
    private Deque<Card> cards;

    @Getter
    private Player host;
    private boolean started;

    @Getter
    private ZonedDateTime creationTime;

    @Getter
    private DiceOrdering diceOrdering;

    @Getter
    private UUID id;

    public GameSession(Player host, UUID id) {
        this.host = host;
        this.id = id;
        players = new ArrayList<>();
        players.add(host);
        diceOrdering = new DiceOrdering(players);
        creationTime = Utils.dateTime();
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public boolean containsMember(Member member) {
        return findPlayer(member) != null;
    }

    public Player findPlayer(Member member) {
        for (var player : players) {
            if (player.getMember().equals(member)) {
                return player;
            }
        }
        return null;
    }

    public void addPlayer(Player player) {
        if (hasStarted()) {
            throw new IllegalStateException("Already started");
        }
        if (players.contains(player)) {
            log.info("Tried to add a player that is already present in this session: {}", player.getName());
            return;
        }
        players.add(player);
    }

    public synchronized boolean hasStarted() {
        return started;
    }

    public synchronized void start() {
        checkStart();
        started = true;
    }

    private void checkStart() {
        if (started) {
            throw new SessionStartException("Already started");
        }
        int size = players.size();
        if (size < MIN_PLAYERS) {
            throw new SessionStartException("Minimum amount of players: " + MIN_PLAYERS + ". Current: " + size);
        }
    }

    public void ordersDefined() {
        if (!diceOrdering.allOrdersDefined()) {
            throw new IllegalStateException("You lied to me!");
        }
        players = diceOrdering.getOrderedList();
        distributeCards();
    }

    public void nextTurn() {
        if (orderCursor == players.size() - 1) {
            orderCursor = 0;
        } else {
            orderCursor++;
        }
    }

    public boolean isYourTurn(Player player) {
        return currentPlayer().equals(player);
    }

    public Player currentPlayer() {
        return players.get(orderCursor);
    }

    private void distributeCards() {
        var cards = new ArrayList<>(Card.cards());
        Collections.shuffle(cards);

        var attackCards = byType(cards, AttackCard.class);
        var defenseCards = byType(cards, DefenseCard.class);

        var removed = new ArrayList<Card>();

        for (var player : players) {
            var pair = List.of(attackCards.removeFirst(), defenseCards.removeFirst());
            removed.addAll(pair);
            player.getHand().addAll(pair);
        }

        cards.removeAll(removed);
        Collections.shuffle(cards);

        this.cards = new ArrayDeque<>(cards);
    }

    private List<Card> byType(List<Card> cards, Class<? extends Card> cardClass) {
        return cards.stream()
                .filter(card -> card.getClass() == cardClass)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
