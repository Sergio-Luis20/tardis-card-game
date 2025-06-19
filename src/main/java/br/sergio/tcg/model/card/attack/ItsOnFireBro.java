package br.sergio.tcg.model.card.attack;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.StatusEffect;
import br.sergio.tcg.model.card.AttackCard;
import br.sergio.tcg.model.card.Rarity;

public class ItsOnFireBro extends AttackCard {

    private int turns;

    public ItsOnFireBro() {
        super("Tá pegando fogo, bicho!", Rarity.COMMON, "Aplica efeito de \"queimadura\", o qual inflige 2 de dano por 7 turnos.", getImage("tá-pegando-fogo-bicho.png"));
    }

    @Override
    public void onAttack(GameSession session, Player attacker, Player defender) {
        var effect = new StatusEffect() {

            Player target = defender;
            int turns = 7;

            @Override
            public void tick() {
                target.subtractHp(2);
                turns--;
                session.log("**" + target.getName() + "** perdeu 2 de vida por queimadura! Vida atual: " + target.getHp() + ".");
            }

            @Override
            public boolean isExpired() {
                return turns <= 0;
            }

        };

        defender.addEffect(effect);
        session.log("**" + defender.getName() + "** foi queimado por **" + attacker.getName() + "**!");
    }

}
