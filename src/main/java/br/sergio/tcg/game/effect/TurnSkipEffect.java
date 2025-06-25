package br.sergio.tcg.game.effect;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.Player;
import lombok.Getter;

@Getter
public class TurnSkipEffect implements StatusEffect {

    private GameSession session;
    private Player target;
    private int turns;

    public TurnSkipEffect(GameSession session, Player target, int turns) {
        Utils.nonNull(session, target);
        this.session = session;
        this.target = target;
        this.turns = turns;
    }

    @Override
    public void tick() {
        session.nextTurn();
        if (turns > 0) {
            turns--;
        }
        session.logf("%s pulou a vez!", target.getBoldName());
    }

    @Override
    public boolean isExpired() {
        return turns == 0;
    }

    @Override
    public boolean permitsDuplicate() {
        return false;
    }

}
