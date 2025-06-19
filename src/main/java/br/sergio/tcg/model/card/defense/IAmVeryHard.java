package br.sergio.tcg.model.card.defense;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.DefenseCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;

public class IAmVeryHard extends DefenseCard {

    public IAmVeryHard() {
        super("Estou durasso!", Rarity.UNCOMMON, "Cria um escudo muito duro (20 de dureza) compartilhado entre esta e outra carta aliada.", getImage("estou-durasso.png"));
    }

    @Override
    public void onDefend(GameSession session, Player attacker, Player defender) {

    }

}
