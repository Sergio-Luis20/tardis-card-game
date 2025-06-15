package br.sergio.tcg.model;

import br.sergio.tcg.game.GameSession;

import java.awt.image.BufferedImage;

public abstract non-sealed class AttackCard extends Card {

    public AttackCard(String name, Rarity rarity, String description, BufferedImage image) {
        super(name, rarity, description, image);
    }

    @Override
    public String getType() {
        return "Ataque";
    }

    public abstract void onAttack(GameSession session, Player attacker, Player defender);

}
