package br.sergio.tcg.model.card.attack;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.StatusEffect;
import br.sergio.tcg.model.card.AttackCard;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.model.card.family.ClownGirlCard;
import br.sergio.tcg.game.event.events.PlayerRevealCardEvent;

import java.util.concurrent.atomic.AtomicBoolean;

public class ClownGirlFurina extends AttackCard implements ClownGirlCard {

    public ClownGirlFurina() {
        super("Garota palhaço Furina", Rarity.EPIC, "Ao ser ativada, esta carta permanece em campo. Uma vez por turno o oponente deve ou não revelar uma carta. Para cada vez que ele revelar uma carta, adicione 1 ao contador da verdade, e para cada vez que ele não revelar, adicione 1 ao contador da mentira. A cada 2 pontos de verdade, compre uma carta; a cada 2 pontos de mentira, o oponente perde 50 pontos de vida.", getImage("garota-palhaço-furina.png"));
    }

    @Override
    public void onAttack(GameSession session, Player attacker, Player defender) {
        var revealed = new AtomicBoolean();

        var effect = new StatusEffect() {

            int truth, lie;

            @Override
            public void tick() {
                if (revealed.get()) {
                    if (++truth % 2 == 0) {
                        session.draw(attacker);
                    }
                } else {
                    if (++lie % 2 == 0) {
                        defender.subtractHp(50);
                    }
                }
                revealed.set(false);
            }

            @Override
            public boolean isExpired() {
                return false;
            }

        };

        defender.addEffect(effect);

        session.listen(PlayerRevealCardEvent.class, (registration, event) -> {
            if (registration.session().isYourTurn(defender) && event.player().equals(defender)) {
                revealed.set(true);
            }
        });

        session.logf("%s aplicou o efeito da garota palhaço Furina em %s!", attacker.getName(), defender.getName());
    }

}
