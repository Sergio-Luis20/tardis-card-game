package br.sergio.tcg.model.card;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.Player;

import java.awt.image.BufferedImage;

public abstract non-sealed class AttackCard extends Card {

    public AttackCard(String name, Rarity rarity, String description, BufferedImage image) {
        super(name, rarity, description, image);
    }

    @Override
    public final String getType() {
        return "Ataque";
    }

    public abstract void onAttack(GameSession session, Player attacker, Player defender);

}
