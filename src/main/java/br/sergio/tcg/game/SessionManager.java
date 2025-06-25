package br.sergio.tcg.game;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
public class SessionManager {

    public static final int TIME_TO_JOIN = 2 * 60;

    private Map<UUID, GameSession> activeSessions;
    private DateTimeFormatter formatter;

    public SessionManager() {
        activeSessions = new ConcurrentHashMap<>();
        formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                .withLocale(Locale.of("pt", "BR"))
                .withZone(ZoneId.of("America/Sao_Paulo"));
    }

    public Optional<GameSession> findSession(Player player) {
        return activeSessions.values().stream()
                .filter(session -> session.getPlayers().contains(player))
                .findAny();
    }

    public Optional<GameSession> findSession(Member member) {
        return activeSessions.values().stream()
                .filter(session -> {
                    for (var player : session.getPlayers()) {
                        if (player.getMember().equals(member)) {
                            return true;
                        }
                    }
                    return false;
                }).findAny();
    }

    public Optional<GameSession> ofHost(Player player) {
        return activeSessions.values().stream()
                .filter(session -> session.getHost().equals(player))
                .findAny();
    }

    public Optional<GameSession> ofHost(Member member) {
        return activeSessions.values().stream()
                .filter(session -> session.getHost().getMember().equals(member))
                .findAny();
    }

    public GameSession createSession(Player owner, MessageChannel gameChannel, int minPlayers, int maxPlayers) throws IllegalSessionCreationException {
        if (findSession(owner).isPresent()) {
            throw new IllegalSessionCreationException("Owner already in a game session");
        }
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (activeSessions.containsKey(id));
        final var sessionId = id;
        var session = new GameSession(owner, sessionId, gameChannel, minPlayers, maxPlayers);
        activeSessions.put(id, session);
        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(TIME_TO_JOIN * 1000);
            } catch (InterruptedException e) {
                log.error("Interrupted while sleeping on session life checker virtual thread", e);
                return;
            }
            synchronized (session) {
                if (!session.hasStarted() && activeSessions.containsKey(sessionId)) {
                    var message = String.format("A partida de %s (%s) demorou muito para começar e foi fechada automaticamente", owner.getBoldName(), sessionId);
                    gameChannel.sendMessage(message).queue(null, t -> log.error("Failed to send timeout message in game channel for match id: {}", sessionId, t));
                    var time = formatter.format(session.getCreationTime());
                    log.info("Session created by {} ({}) at {} didn't start in {} seconds, so it was removed.", owner.getName(), sessionId, time, TIME_TO_JOIN);
                }
            }
        });
        return session;
    }

    public void closeSession(GameSession session) {
        activeSessions.remove(session.getId());
    }

}
