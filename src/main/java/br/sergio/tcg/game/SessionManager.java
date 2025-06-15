package br.sergio.tcg.game;

import br.sergio.tcg.model.Player;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

@Slf4j
@Getter
public class SessionManager {

    private List<GameSession> activeSessions;
    private DateTimeFormatter formatter;

    public SessionManager() {
        activeSessions = Collections.synchronizedList(new ArrayList<>());
        formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                .withLocale(Locale.of("pt", "BR"))
                .withZone(ZoneId.of("America/Sao_Paulo"));
    }

    public Optional<GameSession> findSession(Member member) {
        return activeSessions.stream()
                .filter(session -> session.containsMember(member))
                .findFirst();
    }

    public Optional<GameSession> findSession(Player player) {
        return activeSessions.stream()
                .filter(session -> session.getPlayers().contains(player))
                .findFirst();
    }

    public GameSession createSession(Player owner) throws IllegalSessionCreationException {
        for (var session : activeSessions) {
            if (session.getPlayers().contains(owner)) {
                throw new IllegalSessionCreationException("Owner already in a game session");
            }
        }
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (activeSessions.stream().map(GameSession::getId).toList().contains(id));
        var session = new GameSession(owner, id);
        activeSessions.add(session);
        Thread.startVirtualThread(() -> {
            final int seconds = 60;
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                log.error("Interrupted while sleeping on session life checker virtual thread", e);
                return;
            }
            if (!session.hasStarted() && activeSessions.contains(session)) {
                activeSessions.remove(session);
                var time = formatter.format(session.getCreationTime());
                log.info("Session created by {} at {} didn't start in {} seconds, so it was removed.", owner.getName(), time, seconds);
            }
        });
        return session;
    }

    public void closeSession(GameSession session) {
        activeSessions.remove(session);
    }

}
