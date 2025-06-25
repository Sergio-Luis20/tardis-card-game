package br.sergio.tcg.game;

import br.sergio.tcg.game.effect.StatusEffect;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class LifeOverTurn implements StatusEffect {

    private Player target;
    private int lifeVariation;
    private int turns;
    private UUID effectId;
    private MessageTask messageTask;

    public LifeOverTurn(Player target, int lifeVariation, int turns) {
        this.target = Objects.requireNonNull(target, "target");
        this.lifeVariation = lifeVariation;
        this.turns = turns;
        this.effectId = UUID.randomUUID();
    }

    public LifeOverTurn(Player target, int lifeVariation, int turns, MessageTask messageTask) {
        this(target, lifeVariation, turns);
        this.messageTask = messageTask;
    }

    @Override
    public void tick() {
        if (messageTask != null) {
            messageTask.send();
        }
        target.addHp(lifeVariation);
        turns--;
    }

    @Override
    public boolean isExpired() {
        return turns <= 0;
    }

}
