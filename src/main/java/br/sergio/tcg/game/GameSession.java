package br.sergio.tcg.game;

import br.sergio.tcg.Utils;
import br.sergio.tcg.discord.CardScrollListener;
import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.card.AttackCard;
import br.sergio.tcg.game.card.Card;
import br.sergio.tcg.game.card.CardRepository;
import br.sergio.tcg.game.card.DefaultDefenseCard;
import br.sergio.tcg.game.event.Event;
import br.sergio.tcg.game.event.EventListener;
import br.sergio.tcg.game.event.EventRegistry;
import br.sergio.tcg.game.event.Registration;
import br.sergio.tcg.game.event.events.PlayerDamageEvent;
import br.sergio.tcg.game.query.Query;
import br.sergio.tcg.game.query.QueryCallback;
import br.sergio.tcg.game.query.QueryManager;
import br.sergio.tcg.game.query.queries.ChooseQuery;
import br.sergio.tcg.game.query.queries.SelectOneQuery;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@Slf4j
@Getter
public class GameSession {

    public static final int MIN_PLAYERS = 2, MAX_PLAYERS = 8;

    private UUID id;
    private Player host;
    private List<Player> players;
    private Set<Player> attacked;
    private Map<Player, Boolean> lastTimeAttacked;
    private int orderCursor;
    private volatile TurnDetails currentTurn;
    private boolean started;
    private LinkedList<Card> deck;
    private ZonedDateTime creationTime;
    private DiceOrdering diceOrdering;
    private EventRegistry eventRegistry;
    private QueryManager queryManager;
    private MessageChannel gameChannel;
    private int minPlayers, maxPlayers;
    private Map<Player, CardScrollListener> privateCardReveal;

    public GameSession(Player host, UUID id, MessageChannel gameChannel) {
        this(host, id, gameChannel, MIN_PLAYERS, MAX_PLAYERS);
    }

    public GameSession(Player host, UUID id, MessageChannel gameChannel, int minPlayers, int maxPlayers) {
        this.host = requireNonNull(host, "host");
        this.id = requireNonNull(id, "id");
        this.gameChannel = requireNonNull(gameChannel, "gameChannel");

        this.minPlayers = Math.max(minPlayers, MIN_PLAYERS);
        this.maxPlayers = Math.min(maxPlayers, MAX_PLAYERS);

        players = Collections.synchronizedList(new ArrayList<>());
        players.add(host);
        attacked = ConcurrentHashMap.newKeySet();
        privateCardReveal = new ConcurrentHashMap<>();
        lastTimeAttacked = new ConcurrentHashMap<>();
        lastTimeAttacked.put(host, false);

        diceOrdering = new DiceOrdering(players);
        eventRegistry = new EventRegistry(this);
        queryManager = new QueryManager(this);
        creationTime = Utils.dateTime();
    }

    public void sendCardToDeck(Player player, Card card) {
        player.getHand().remove(card);
        player.getTheVoid().remove(card);
        if (card == DefaultDefenseCard.INSTANCE) {
            return;
        }
        if (deck.isEmpty()) {
            deck.add(card);
        } else {
            int index = ThreadLocalRandom.current().nextInt(deck.size());
            deck.add(index, card);
        }
    }

    public boolean containsMember(Member member) {
        return findByMember(member).isPresent();
    }

    public Optional<Player> findByMember(Member member) {
        return players.stream()
                .filter(player -> player.getMember().equals(member))
                .findAny();
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
        gameChannel.sendMessage(message).queue(null, t -> {
            log.error("Failed to send a message to the game channel", t);
        });
    }

    public void logf(String format, Object... args) {
        log(String.format(format, args));
    }

    public void showCard(Player cardOwner, Card card) {
        var cardEmbed = DiscordService.getInstance().getEmbedFactory().createCardEmbed(cardOwner, card);
        if (cardOwner != null) {
            gameChannel.sendMessage(cardOwner.getBoldName() + " jogou a seguinte carta:")
                    .setEmbeds(cardEmbed).queue(null, t -> log.error("Failed to send " +
                            "card show message of {}", cardOwner.getName(), t));
        } else {
            gameChannel.sendMessageEmbeds(cardEmbed).queue(null, t -> log.error("Failed to show card embed", t));
        }
    }

    public void revealCards(Player revealer, List<Card> cards) {
        // revela cartas publicamente para todos no chat do jogo
        if (cards.isEmpty()) {
            if (revealer == null) {
                log("Nenhuma carta foi revelada!");
            } else {
                logf("Nenhuma carta de %s foi revelada!", revealer.getBoldName());
            }
            return;
        }
        var cardEmbeds = cards.stream()
                .map(card -> DiscordService.getInstance().getEmbedFactory().createCardEmbed(revealer, card))
                .toList();
        if (revealer == null) {
            gameChannel.sendMessage("A(s) seguinte(s) carta(s) foi(ram) revelada(s):")
                    .setEmbeds(cardEmbeds).queue(null, t -> log.error("Failed to send " +
                            "reveal cards message of {}", null, t));
        } else {
            gameChannel.sendMessage("A(s) seguinte(s) carta(s) de " + revealer.getBoldName()
                    + " foi(ram) revelada(s):").setEmbeds(cardEmbeds).queue(null,
                    t -> log.error("Failed to reveal cards of {}", revealer.getName(), t));
        }
    }

    public void revealCards(Player revealer, List<Card> cards, Set<Player> viewers) {
        // revela cartas privadamente apenas para os jogadores do set
        if (cards.isEmpty()) {
            if (viewers.isEmpty()) {
                if (revealer == null) {
                    log("Nenhuma carta foi revelada!");
                } else {
                    logf("Nenhuma carta de %s foi revelada!", revealer.getBoldName());
                }
                return;
            }
            for (var viewer : viewers) {
                viewer.getMember().getUser().openPrivateChannel().queue(pv -> {
                    if (revealer == null) {
                        pv.sendMessage("Nenhuma carta foi revelada para você!").queue();
                    } else {
                        pv.sendMessage("Nenhuma carta de " + revealer.getBoldName() + " foi revelada para você!").queue();
                    }
                }, t -> log.error("Failed to retrieve private channel of {}", viewer.getName(), t));
            }
            return;
        }
        if (viewers.isEmpty()) {
            revealCards(revealer, cards);
            return;
        }
        var revealedCards = new ArrayList<>(cards);
        var service = DiscordService.getInstance();
        var firstEmbed = service.getEmbedFactory().createCardEmbed(revealer, cards.getFirst());
        for (var viewer : viewers) {
            viewer.getMember().getUser().openPrivateChannel().queue(pv -> {
                var interactionId = UUID.randomUUID().toString();
                var message = revealer == null ? "A(s) seguinte(s) carta(s) foi(ram) revelada(s):"
                        : "A(s) seguinte(s) carta(s) de " + revealer.getBoldName()
                        + " foi(ram) revelada(s) para você:";
                var oldListener = privateCardReveal.remove(viewer);
                if (oldListener != null) {
                    oldListener.cleanup();
                }
                pv.sendMessage(message)
                        .setEmbeds(firstEmbed)
                        .setActionRow(CardScrollListener.makeButtons(interactionId, revealedCards))
                        .queue(revealMessage -> {
                            var jda = service.getJda();
                            var listener = new CardScrollListener(interactionId, viewer, revealMessage,
                                    jda, revealedCards, privateCardReveal);
                            privateCardReveal.put(viewer, listener);
                        }, t -> {
                            log.error("Failed to send reveal cards message of {} to {}", revealer, viewer.getName());
                            logf("Não consigo te mandar mensagens no privado, %s", viewer.getMember().getAsMention());
                        });
            }, t -> {
                log.error("Failed to retrieve private channel of {}", viewer.getName(), t);
                logf("Não consigo acessar seu privado, %s", viewer.getMember().getAsMention());
            });
        }
    }

    public <E extends Event> Registration<E> listen(Class<E> eventClass, EventListener<E> listener) {
        return eventRegistry.register(eventClass, listener);
    }

    public <Q extends Query<R>, R> void query(Q query, QueryCallback<Q, R> callback) {
        queryManager.query(query, callback);
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
        lastTimeAttacked.put(player, false);
    }

    public Card draw(Player player) {
        var card = deck.poll();
        if (card == null) {
            log.warn("Empty deck");
        } else {
            player.getHand().add(card);
        }
        return card;
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
        startTurn();
    }

    public void startTurn() {
        var player = currentPlayer();
        if (player == null) {
            logf("Todos os jogadores morreram ou saíram da partida. Fim de jogo");
            DiscordService.getInstance().getSessionManager().closeSession(this);
            return;
        }
        player.processEffects();
        var playerIterator = players.iterator();
        while (playerIterator.hasNext()) {
            var p = playerIterator.next();
            if (p.isDead()) {
                playerIterator.remove();
                logf("%s morreu!");
            }
        }
        if (players.isEmpty()) {
            logf("Todos os jogadores morreram ou saíram da partida. Fim de jogo");
            DiscordService.getInstance().getSessionManager().closeSession(this);
            return;
        }
        if (players.size() == 1) {
            var winner = players.getFirst();
            logf("%s venceu a partida!", winner.getBoldName());
            DiscordService.getInstance().getSessionManager().closeSession(this);
            return;
        }
        if (player != currentPlayer()) {
            logf("%s pulou o turno!", player.getBoldName());
            Thread.startVirtualThread(this::startTurn);
            return;
        }
        if (attacked.containsAll(players)) {
            attacked.clear();
            log("Todos compram 1 carta!");
            for (var p : players) {
                draw(p);
            }
        }
        logf("Iniciando turno de %s.", player.getBoldName());
        if (player.getHand().isEmpty()) {
            var damage = new AttributeInstance(50);
            var event = new PlayerDamageEvent(null, player, damage);
            eventRegistry.callEvent(event);
            int damageValue = (int) event.getDamage().calculate();
            player.subtractHp(damageValue);
            lastTimeAttacked.put(player, false);
            if (draw(player) != null) {
                logf("%s não tinha cartas na mão, portanto perdeu %d de vida, comprou uma carta e " +
                        "pulou a vez.", player.getBoldName(), damageValue);
            } else {
                logf("%s não tinha cartas na mão, portanto perdeu %d de vida e pulou a vez (não havia " +
                        "mais cartas no deck para comprar).", player.getBoldName(), damageValue);
            }
            nextTurn();
            startTurn();
            return;
        }
        if (player.getHand().stream().noneMatch(AttackCard.class::isInstance)) {
            if (lastTimeAttacked.get(player)) {
                lastTimeAttacked.put(player, false);
                logf("%s não possui cartas de ataque, portanto em seu turno ele obrigatoriamente comprou uma carta.");
                currentTurn = TurnDetails.pacific(this, player);
                currentTurn.startTurn();
            } else {
                if (draw(player) != null) {
                    logf("%s não possui cartas de ataque numa rodada em que deve atacar, portanto " +
                            "comprou uma carta e passou a vez.", player.getBoldName());
                } else {
                    logf("%s não possui cartas de ataque numa rodada em que deve atacar, portanto pulou " +
                            "a vez (não havia mais cartas no deck para comprar).", player.getBoldName());
                }
                nextTurn();
                startTurn();
            }
            return;
        }
        final var attack = "\uD83D\uDD2A Atacar";
        final var drawCard = "\uD83C\uDCCF Comprar carta";
        var query = new ChooseQuery<>(
                player,
                "Sua vez, " + player.getBoldName() + "! Escolha o que fazer:",
                List.of(attack, drawCard),
                false,
                Function.identity()
        );
        query(query, (q, action) -> {
            switch (action) {
                case attack -> {
                    var players = new ArrayList<>(getPlayers());
                    players.remove(player);
                    var playerQuery = new SelectOneQuery<>(
                            player,
                            "Escolha quem atacar",
                            players,
                            false,
                            false,
                            DiscordService.getInstance().getEmbedFactory()::createPlayerEmbed
                    );
                    query(playerQuery, (pq, target) -> {
                        currentTurn = TurnDetails.battle(this, player, target);
                        currentTurn.startTurn();
                    });
                    lastTimeAttacked.put(player, true);
                    attacked.add(player);
                }
                case drawCard -> {
                    currentTurn = TurnDetails.pacific(this, player);
                    currentTurn.startTurn();
                }
                default -> throw new AssertionError("No recognized option on choose query.");
            }
        });
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

        var attackCards = repo.getAttackCards();
        var defenseCards = repo.getDefenseCards();
        var effectCards = repo.getEffectCards();

        Collections.shuffle(attackCards);
        Collections.shuffle(defenseCards);
        Collections.shuffle(effectCards);

        var removed = new ArrayList<Card>();

        for (var player : players) {
            var pair = List.of(attackCards.removeFirst(), defenseCards.removeFirst(), effectCards.removeFirst());
            removed.addAll(pair);
            player.getHand().addAll(pair);
        }

        cards.removeAll(removed);
        Collections.shuffle(cards);

        this.deck = new LinkedList<>(cards);
    }

}
