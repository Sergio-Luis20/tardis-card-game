package br.sergio.tcg.game.card.effect;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.card.family.FoodCard;
import br.sergio.tcg.game.effect.FlatHealTurnEffect;

import java.util.concurrent.CompletableFuture;

public class JucurutuCookie extends EffectCard implements FoodCard {

    public JucurutuCookie() {
        super("Bolacha Jucurutu", Rarity.UNCOMMON, "Cura 10 de vida por 3 turnos.",
                "https://i.imgur.com/WzLvVNW.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var target = turn.getPlayer();
        var effect = new FlatHealTurnEffect(target, turn.getSession(), 3, 10);
        target.addEffect(effect);
        turn.logf("%s comeu uma bolacha jucurutu e ganhou efeito de cura contínua por 3 turnos!", target.getBoldName());
        return completedAction();
    }

}
