package br.sergio.tcg.game.effect;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.AttributeInstance;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.Player;

public class FlatHealTurnEffect implements HealEffect {

    private AttributeInstance heal;
    private Player target;
    private GameSession session;
    private int turns;

    public FlatHealTurnEffect(Player target, GameSession session, int turns, int heal) {
        Utils.nonNull(target, session);
        this.target = target;
        this.session = session;
        this.turns = turns;
        this.heal = new AttributeInstance(heal);
    }

    @Override
    public AttributeInstance getAttribute() {
        return heal;
    }

    @Override
    public void tick() {
        int heal = (int) this.heal.calculate();
        target.addHp(heal);
        session.logf("%s regenerou %d de vida!", target.getBoldName(), heal);
    }

    @Override
    public boolean isExpired() {
        return turns <= 0;
    }

}
