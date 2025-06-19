package br.sergio.tcg.model.card.effect;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.EffectCard;
import br.sergio.tcg.model.card.Rarity;
import br.sergio.tcg.model.StatusEffect;
import br.sergio.tcg.game.event.events.PlayerDamageEvent;

import java.util.concurrent.atomic.AtomicBoolean;

public class BileuMisera extends EffectCard {

    public BileuMisera() {
        super("BileuMisera", Rarity.COMMON, "Cura 5 de vida por 5 turnos. Se você sofrer algum dano, ganha o efeito \"hipertensão\", que faz perder 2 de vida pelos turnos restantes.", getImage("bileumisera.png"));
    }

    @Override
    public void onEffect(GameSession session, Player player) {
        final AtomicBoolean flag = new AtomicBoolean();

        StatusEffect hypertension = new StatusEffect() {

            int turns = 5;
            AtomicBoolean damaged = flag;

            @Override
            public void tick() {
                if (damaged.get()) {
                    player.subtractHp(5);
                    session.logf("%s tomou 5 de dano por hipertensão!", player.getName());
                } else {
                    player.addHp(5);
                    session.logf("%s regenerou 5 de vida!", player.getName());
                }
                turns--;
            }

            @Override
            public boolean isExpired() {
                return turns <= 0;
            }

        };

        player.addEffect(hypertension);

        session.listen(PlayerDamageEvent.class, (registration, event) -> {
            if (event.target().equals(player)) {
                flag.set(true);
                registration.cancel();
            }
        });

        session.logf("%s ganhou o efeito \"BileuMisera\"!", player.getName());
    }

}
