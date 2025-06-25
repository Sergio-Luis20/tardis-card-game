package br.sergio.tcg.game.card;

import java.awt.image.BufferedImage;

public abstract non-sealed class DefenseCard extends Card {

    public DefenseCard(String name, Rarity rarity, String description, BufferedImage image) {
        super(name, rarity, description, image);
    }

    @Override
    public final String getType() {
        return "Defesa";
    }

}
