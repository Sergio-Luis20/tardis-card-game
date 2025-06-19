package br.sergio.tcg.model.card;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.Player;

import java.awt.image.BufferedImage;

public abstract non-sealed class DefenseCard extends Card {

    public DefenseCard(String name, Rarity rarity, String description, BufferedImage image) {
        super(name, rarity, description, image);
    }

    @Override
    public final String getType() {
        return "Defesa";
    }

    public abstract void onDefend(GameSession session, Player attacker, Player defender);

}
