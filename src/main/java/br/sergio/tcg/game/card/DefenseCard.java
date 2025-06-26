package br.sergio.tcg.game.card;

public abstract non-sealed class DefenseCard extends Card {

    public DefenseCard(String name, Rarity rarity, String description, String imageUrl) {
        super(name, rarity, description, imageUrl);
    }

    @Override
    public final String getType() {
        return "Defesa";
    }

}
