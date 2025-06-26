package br.sergio.tcg.game.card;

public abstract non-sealed class EffectCard extends Card {

    public EffectCard(String name, Rarity rarity, String description, String imageUrl) {
        super(name, rarity, description, imageUrl);
    }

    @Override
    public final String getType() {
        return "Efeito";
    }

}
