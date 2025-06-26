package br.sergio.tcg.game.card.defense;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.DefenseCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.effect.FlatShieldEffect;

import java.util.concurrent.CompletableFuture;

public class IAmVeryHard extends DefenseCard {

    public IAmVeryHard() {
        super("Estou durasso!", Rarity.UNCOMMON, "Cria um escudo muito duro (20 de dureza).",
                "https://i.imgur.com/qJJ9wwl.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        turn.resetBattle(true);
        var target = turn.getDefender();
        var hardShield = new FlatShieldEffect(target, turn.getSession(), 20);
        turn.getBattleDetails().addEffect(target, hardShield);
        turn.logf("%s está durasso e ganhou um escudo tão duro quanto!", target.getBoldName());
        return completedAction();
    }

}
