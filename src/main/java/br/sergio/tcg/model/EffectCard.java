package br.sergio.tcg.model;

import br.sergio.tcg.game.GameSession;

import java.awt.image.BufferedImage;

public abstract non-sealed class EffectCard extends Card {

    public EffectCard(String name, Rarity rarity, String description, BufferedImage image) {
        super(name, rarity, description, image);
    }

    @Override
    public String getType() {
        return "Efeito";
    }

    public abstract void onEffect(GameSession session, Player player);

}
