package br.sergio.tcg.game.card.effect;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.card.family.ClownGirlCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class IWantClownGirls extends EffectCard implements ClownGirlCard {

    public IWantClownGirls() {
        super("Eu queria garotas palhaço", Rarity.COMMON, "Adiciona do deck à sua mão uma " +
                "carta \"garota palhaço\".", "https://i.imgur.com/nS7SEQp.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var session = turn.getSession();
        var deck = session.getDeck();
        var clownGirls = deck.stream()
                .filter(ClownGirlCard.class::isInstance)
                .collect(Collectors.toCollection(ArrayList::new));
        if (clownGirls.isEmpty()) {
            turn.log("Não há mais cartas \"garota palhaço\" no deck.");
            return completedAction();
        }
        Collections.shuffle(clownGirls);
        var card = clownGirls.get(ThreadLocalRandom.current().nextInt(clownGirls.size()));
        deck.remove(card);
        var player = turn.getPlayer();
        player.getHand().add(card);
        session.logf("%s adquiriu uma carta \"garota palhaço\"!", player.getBoldName());
        return completedAction();
    }

}
