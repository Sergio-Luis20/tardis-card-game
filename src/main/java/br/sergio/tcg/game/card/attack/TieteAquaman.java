package br.sergio.tcg.game.card.attack;

import br.sergio.tcg.game.AttributeInstance;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.AttackCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.effect.DoTTurnEffect;

import java.util.concurrent.CompletableFuture;

public class TieteAquaman extends AttackCard {

    public TieteAquaman() {
        super("Aquaman do Tietê", Rarity.COMMON, "Aplica veneno que dá 4 de dano por 5 " +
                "turnos.", "https://i.imgur.com/WoHfC10.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var target = turn.getDefender();
        var message = target.getBoldName() + " recebeu 4 de dano de veneno pelo Aquaman do Tietê!";
        var poison = new DoTTurnEffect(target, 5, new AttributeInstance(4), turn.getSession(), message);
        turn.getBattleDetails().addEffect(target, poison);
        turn.logf("%s aplicou o efeito de veneno do Aquaman do Tietê em %s!", turn.getAttacker().getBoldName(), target.getBoldName());
        return completedAction();
    }

}
