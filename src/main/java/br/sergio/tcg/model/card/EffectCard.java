package br.sergio.tcg.model.card;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.Player;

import java.awt.image.BufferedImage;

public abstract non-sealed class EffectCard extends Card {

    public EffectCard(String name, Rarity rarity, String description, BufferedImage image) {
        super(name, rarity, description, image);
    }

    @Override
    public final String getType() {
        return "Efeito";
    }

    public abstract void onEffect(GameSession session, Player player);

}
