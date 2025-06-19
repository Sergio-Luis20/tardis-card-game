package br.sergio.tcg.model.card.defense;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.DefenseCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.model.card.family.ClownGirlCard;

public class ClownGirlNeuro extends DefenseCard implements ClownGirlCard {

    public ClownGirlNeuro() {
        super("Garota palhaço Neuro", Rarity.UNCOMMON, "Escolha quaisquer cartas da sua mão e revele ao oponente. Para cada carta revelada, ganhe 10 pontos de vida.", getImage("garota-palhaço-neuro.png"));
    }

    @Override
    public void onDefend(GameSession session, Player attacker, Player defender) {

    }

}
