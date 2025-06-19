package br.sergio.tcg.discord;

import br.sergio.tcg.Utils;
import br.sergio.tcg.model.Player;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;
import java.util.UUID;

@Getter
public class DiscordPlayer extends Player {

    private Member member;

    public DiscordPlayer(UUID id, Member member, DiscordGameSession session) {
        this(id, member, Utils.randomColor(), session);
    }

    public DiscordPlayer(UUID id, Member member, Color color, DiscordGameSession session) {
        super(id, member.getEffectiveName(), color, session);
        this.member = member;
    }

    public String getBoldName() {
        return "**" + getName() + "**";
    }

    public String getAvatarUrl() {
        return member.getEffectiveAvatarUrl();
    }

    public DiscordGameSession getSession() {
        return (DiscordGameSession) super.getSession();
    }

}
