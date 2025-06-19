package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.model.card.family.RevelationCard;

public class Cryptography extends EffectCard implements RevelationCard {

    public Cryptography() {
        super("Criptografia", Rarity.UNCOMMON, "Se alguma carta de revelação for jogada em campo, você pode secretamente jogar esta carta e ocultar a verdadeira face da carta.", getImage("criptografia.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {

    }

}
