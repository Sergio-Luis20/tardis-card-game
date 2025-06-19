package br.sergio.tcg.discord;

import br.sergio.tcg.game.GameSession;
import net.dv8tion.jda.api.entities.Member;

import java.util.Optional;
import java.util.UUID;

public class DiscordGameSession extends GameSession {

    public DiscordGameSession(DiscordPlayer host, UUID id) {
        super(host, id, DiscordService.getInstance());
    }

    @Override
    public DiscordPlayer getHost() {
        return (DiscordPlayer) super.getHost();
    }

    public boolean containsMember(Member member) {
        return findByMember(member).isPresent();
    }

    public Optional<DiscordPlayer> findByMember(Member member) {
        return getPlayers().stream()
                .map(DiscordPlayer.class::cast)
                .filter(player -> player.getMember().equals(member))
                .findAny();
    }

}
