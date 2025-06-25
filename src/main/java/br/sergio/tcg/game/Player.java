package br.sergio.tcg.game;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.card.Card;
import br.sergio.tcg.game.effect.StatusEffect;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Player {

    @Include
    private Member member;
    private Color color;
    private List<Card> hand, theVoid, history;
    private List<StatusEffect> effects;
    private int hp;

    public Player(Member member) {
        this(member, Utils.randomColor());
    }

    public Player(Member member, Color color) {
        this.member = requireNonNull(member, "member");
        this.color = requireNonNull(color, "color");

        hand = Collections.synchronizedList(new ArrayList<>());
        theVoid = Collections.synchronizedList(new ArrayList<>());
        history = Collections.synchronizedList(new ArrayList<>());
        effects = Collections.synchronizedList(new ArrayList<>());

        hp = 1000;
    }

    public synchronized void sendCardToTheVoid(Card card) {
        hand.remove(card);
        theVoid.add(card);
    }

    public synchronized void addEffect(StatusEffect effect) {
        if (!effect.permitsDuplicate() && effects.stream().anyMatch(e -> e.getClass() == effect.getClass())) {
            return;
        }
        effects.add(effect);
        effect.whenApplied();
    }

    public synchronized void removeEffect(StatusEffect effect) {
        effects.remove(effect);
        effect.whenRemoved();
    }

    public synchronized void processEffects() {
        var iter = effects.iterator();
        while (iter.hasNext()) {
            var effect = iter.next();
            effect.tick();
            if (effect.isExpired()) {
                iter.remove();
                effect.whenRemoved();
            }
        }
    }

    public synchronized List<StatusEffect> getEffects() {
        return Collections.unmodifiableList(effects);
    }

    public synchronized <T extends StatusEffect> List<T> getEffects(Class<T> effectType) {
        return effects.stream()
                .filter(effectType::isInstance)
                .map(effectType::cast)
                .toList();
    }

    public String getName() {
        return member.getEffectiveName();
    }

    public String getBoldName() {
        return "**" + getName() + "**";
    }

    public String getAvatarUrl() {
        return member.getEffectiveAvatarUrl();
    }

    public synchronized void subtractHp(int hp) {
        setHp(this.hp - hp);
    }

    public synchronized void addHp(int hp) {
        setHp(this.hp + hp);
    }

    public synchronized void setHp(int hp) {
        this.hp = Math.max(0, hp);
    }

    public synchronized boolean isDead() {
        return hp <= 0;
    }

}
