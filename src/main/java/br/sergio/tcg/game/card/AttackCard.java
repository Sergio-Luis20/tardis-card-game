package br.sergio.tcg.game.card;

public abstract non-sealed class AttackCard extends Card {

    public AttackCard(String name, Rarity rarity, String description, String imageUrl) {
        super(name, rarity, description, imageUrl);
    }

    @Override
    public final String getType() {
        return "Ataque";
    }

}
