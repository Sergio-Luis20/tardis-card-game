package br.sergio.tcg.game;

import br.sergio.tcg.InterfaceDriver;
import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.model.Player;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
public class SessionManager {

    private Map<UUID, GameSession> activeSessions;
    private DateTimeFormatter formatter;
    private InterfaceDriver driver;

    public SessionManager(InterfaceDriver driver) {
        this.driver = Objects.requireNonNull(driver, "driver");
        activeSessions = new ConcurrentHashMap<>();
        formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                .withLocale(Locale.of("pt", "BR"))
                .withZone(ZoneId.of("America/Sao_Paulo"));
    }

    public Optional<GameSession> findSession(Player player) {
        return activeSessions.values().stream()
                .filter(session -> session.getPlayers().contains(player))
                .findFirst();
    }

    public Optional<GameSession> ofHost(Player player) {
        return activeSessions.values().stream()
                .filter(session -> session.getHost().equals(player))
                .findFirst();
    }

    public GameSession createSession(Player owner) throws IllegalSessionCreationException {
        for (var session : activeSessions.values()) {
            if (session.getPlayers().contains(owner)) {
                throw new IllegalSessionCreationException("Owner already in a game session");
            }
        }
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (activeSessions.containsKey(id));
        var session = driver.createSession(owner, id);
        activeSessions.put(id, session);
        Thread.startVirtualThread(() -> {
            final int seconds = 60;
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                log.error("Interrupted while sleeping on session life checker virtual thread", e);
                return;
            }
            synchronized (session) {
                var sessionId = session.getId();
                if (!session.hasStarted() && activeSessions.containsKey(sessionId)) {
                    activeSessions.remove(sessionId);
                    DiscordService.getInstance().getGameChannel().sendMessage("A partida de **" + session.getHost().getName() + "** (" + session.getId() + ") demorou muito para começar e foi fechada automaticamente.").queue();
                    var time = formatter.format(session.getCreationTime());
                    log.info("Session created by {} at {} didn't start in {} seconds, so it was removed.", owner.getName(), time, seconds);
                }
            }
        });
        return session;
    }

    public void closeSession(GameSession session) {
        activeSessions.remove(session.getId());
    }

}
