package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.model.card.family.ClownGirlCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class IWantClownGirls extends EffectCard implements ClownGirlCard {

    public IWantClownGirls() {
        super("Eu queria garotas palhaço", Rarity.COMMON, "Adiciona do deck à sua mão uma carta \"garota palhaço\".", getImage("eu-queria-garotas-palhaço.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {
        var cards = session.getCards();
        var clownGirls = cards.stream()
                .filter(ClownGirlCard.class::isInstance)
                .collect(Collectors.toCollection(ArrayList::new));
        if (clownGirls.isEmpty()) {
            return;
        }
        Collections.shuffle(clownGirls);
        var card = clownGirls.getFirst();
        cards.remove(card);
        player.getHand().add(card);
        session.log("**" + player.getName() + "** adquiriu uma carta \"garota palhaço\"!");
    }

}
