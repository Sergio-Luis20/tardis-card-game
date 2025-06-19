package br.sergio.tcg.game;

import br.sergio.tcg.GameLogicException;
import br.sergio.tcg.InterfaceDriver;
import br.sergio.tcg.Utils;
import br.sergio.tcg.game.event.Event;
import br.sergio.tcg.game.event.EventListener;
import br.sergio.tcg.game.event.EventRegistry;
import br.sergio.tcg.game.event.Registration;
import br.sergio.tcg.game.query.Query;
import br.sergio.tcg.game.query.QueryCallback;
import br.sergio.tcg.game.query.QueryManager;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Card;
import br.sergio.tcg.model.card.CardRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
@Getter
public class GameSession {

    private InterfaceDriver driver;
    private UUID id;
    private Player host;
    private List<Player> players;
    private int orderCursor;
    private boolean started;
    private Deque<Card> cards;
    private ZonedDateTime creationTime;
    private DiceOrdering<Player> diceOrdering;
    private Map<Player, List<Card>> cemetery;
    private EventRegistry eventRegistry;
    private QueryManager queryManager;
    private int minPlayers, maxPlayers;

    public GameSession(Player host, UUID id, InterfaceDriver driver) {
        this.host = requireNonNull(host, "host");
        this.id = requireNonNull(id, "id");
        this.driver = requireNonNull(driver, "driver");

        minPlayers = 4;
        maxPlayers = 8;

        players = Collections.synchronizedList(new ArrayList<>());
        players.add(host);

        cemetery = new HashMap<>();
        diceOrdering = new DiceOrdering<>(players);
        eventRegistry = new EventRegistry(this);
        queryManager = new QueryManager(this);
        creationTime = Utils.dateTime();
    }

    public synchronized void configurePlayerAmount(int minPlayers, int maxPlayers) {
        if (hasStarted()) {
            throw new IllegalStateException("Already started");
        }
        if (minPlayers < 2) {
            throw new IllegalArgumentException("minPlayers must be at least 2");
        }
        if (maxPlayers < minPlayers) {
            throw new IllegalArgumentException("maxPlayers must be at least the same number of minPlayers");
        }
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public void log(String message) {
        driver.sendMessage(this, message).exceptionally(t -> {
            log.error("Failed to send a message to the game channel", t);
            return null;
        });
    }

    public void logf(String format, Object... args) {
        log(String.format(format, args));
    }

    public <E extends Event> Registration<E> listen(Class<E> eventClass, EventListener<E> listener) {
        return eventRegistry.register(eventClass, listener);
    }

    public <Q extends Query<R>, R> void query(Q query, QueryCallback<Q, R> callback) {
        queryManager.query(query, callback);
    }

    public void kill(Player player, Card deadCard) {
        if (!player.getHand().remove(deadCard)) {
            throw new GameLogicException("Card \"" + deadCard.getName() + "\" wasn't in " + player.getName() + "' hand.");
        }
        cemetery.get(player).add(deadCard);
    }

    public void resurrect(Player player, Card deadCard) {
        if (!cemetery.get(player).remove(deadCard)) {
            throw new GameLogicException("Card \"" + deadCard.getName() + "\" wasn't in " + player.getName() + "' cemetery.");
        }
        player.getHand().add(deadCard);
    }

    public void addPlayer(Player player) {
        if (hasStarted()) {
            throw new IllegalStateException("Already started");
        }
        if (players.contains(player)) {
            log.warn("Tried to add the player \"{}\", but it is already present in this session.", player.getName());
            return;
        }
        if (players.size() >= maxPlayers) {
            log.warn("Tried to add the player \"{}\", but this match is full.", player.getName());
            return;
        }
        players.add(player);
        cemetery.put(player, new ArrayList<>());
    }

    public Optional<Player> findById(UUID playerId) {
        return players.stream()
                .filter(player -> player.getId().equals(playerId))
                .findAny();
    }

    public void draw(Player player) {
        var card = cards.poll();
        if (card == null) {
            log.warn("Empty deck");
            return;
        }
        player.getHand().add(card);
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
        if (size < minPlayers) {
            throw new SessionStartException("Minimum amount of players: " + minPlayers + ". Current: " + size);
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
        var repo = CardRepository.getInstance();
        var cards = repo.getCards();
        Collections.shuffle(cards);

        var attackCards = repo.getAttackCards();
        var defenseCards = repo.getDefenseCards();

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

}
