package br.sergio.tcg.model;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.Card;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;

import java.awt.*;
import java.util.*;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Player {

    @Include
    private UUID id;
    private String name;
    private Color color;
    private List<Card> hand;
    private List<StatusEffect> effects;
    private GameSession session;
    private int hp;

    public Player(UUID id, String name, GameSession session) {
        this(id, name, Utils.randomColor(), session);
    }

    public Player(UUID id, String name, Color color, GameSession session) {
        this.id = requireNonNull(id, "id");
        this.name = requireNonNull(name, "name");
        this.color = requireNonNull(color, "color");
        this.session = requireNonNull(session, "session");

        hand = Collections.synchronizedList(new ArrayList<>());
        effects = Collections.synchronizedList(new ArrayList<>());

        hp = 1000;
    }

    public synchronized void addEffect(StatusEffect effect) {
        effects.add(effect);
    }

    public synchronized void processEffects() {
        effects.forEach(StatusEffect::tick);
        effects.removeIf(StatusEffect::isExpired);
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
