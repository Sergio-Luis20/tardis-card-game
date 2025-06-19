package br.sergio.tcg.model.card.attack;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.query.queries.RevealCardsQuery;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.AttackCard;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.model.card.family.ClownGirlCard;

public class ClownGirlEvil extends AttackCard implements ClownGirlCard {

    public ClownGirlEvil() {
        super("Garota palhaço Evil", Rarity.UNCOMMON, "Escolha quaisquer cartas da sua mão e revele ao oponente. Para cada carta revelada, cause 10 pontos de dano à vida dele.", getImage("garota-palhaço-evil.png"));
    }

    @Override
    public void onAttack(GameSession session, Player attacker, Player defender) {
        var query = new RevealCardsQuery(attacker, defender);
        session.query(query, (q, result) -> {
            int revealed = result.size();
            if (revealed == 0) {
                session.logf("%s se recusou a causar dano a %s", attacker.getName(), defender.getName());
                return;
            }
            int damage = 10 * revealed;
            defender.subtractHp(damage);
            session.logf("%s revelou %d cartas para %s, causando %d de dano no processo!", attacker.getName(), revealed, defender.getName(), damage);
        });
    }

}
