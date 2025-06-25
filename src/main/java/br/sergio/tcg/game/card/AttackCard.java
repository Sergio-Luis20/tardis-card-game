package br.sergio.tcg.game.card;

import java.awt.image.BufferedImage;

public abstract non-sealed class AttackCard extends Card {

    public AttackCard(String name, Rarity rarity, String description, BufferedImage image) {
        super(name, rarity, description, image);
    }

    @Override
    public final String getType() {
        return "Ataque";
    }

}
