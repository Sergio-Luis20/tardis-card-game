package br.sergio.tcg.model;

import br.sergio.tcg.Utils;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Player {

    @Include
    private Member member;
    private Color color;
    private List<Card> hand;
    private int hp;

    public Player(Member member) {
        this(member, Utils.randomColor());
    }

    public Player(Member member, Color color) {
        this.member = member;
        this.color = color;

        hand = new ArrayList<>();
        hp = 1000;
    }

    public void subtractHp(int hp) {
        setHp(this.hp - hp);
    }

    public void addHp(int hp) {
        setHp(this.hp + hp);
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, hp);
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public String getName() {
        return member.getEffectiveName();
    }

    public String getAvatarUrl() {
        return member.getEffectiveAvatarUrl();
    }

}
