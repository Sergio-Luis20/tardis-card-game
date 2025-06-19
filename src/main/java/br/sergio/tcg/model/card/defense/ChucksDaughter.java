package br.sergio.tcg.model.card.defense;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.DefenseCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;

public class ChucksDaughter extends DefenseCard {

    public ChucksDaughter() {
        super("Filha do Chuck", Rarity.COMMON, "Ganha para si o efeito \"vergonha alheia\" por 2 turnos. Enquanto estiver com esse efeito, o inimigo perde no argumento, reduzindo o dano recebido por cartas aliadas em 20%.", getImage("filha-do-chuck.png"));
    }

    @Override
    public void onDefend(GameSession session, Player attacker, Player defender) {

    }

}
