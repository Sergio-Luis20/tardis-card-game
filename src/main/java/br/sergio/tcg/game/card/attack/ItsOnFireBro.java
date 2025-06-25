package br.sergio.tcg.game.card.attack;

import br.sergio.tcg.game.AttributeInstance;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.AttackCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.effect.DoTTurnEffect;

import java.util.concurrent.CompletableFuture;

public class ItsOnFireBro extends AttackCard {

    private int turns;

    public ItsOnFireBro() {
        super("Tá pegando fogo, bicho!", Rarity.COMMON, "Aplica efeito de \"queimadura\", " +
                "que inflige 2 de dano por 7 turnos.", getImage("tá-pegando-fogo-bicho.png"));
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var target = turn.getDefender();
        var message = target.getBoldName() + " perdeu 2 de vida por queimadura! Vida atual: " + target.getHp() + ".";
        var burning = new DoTTurnEffect(target, 7, new AttributeInstance(2), turn.getSession(), message);
        turn.getBattleDetails().addEffect(target, burning);
        return completedAction();
    }

}
