package br.sergio.tcg.model;

public enum Rarity {

    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY;

    public String translated() {
        return switch (this) {
            case COMMON -> "Comum";
            case UNCOMMON -> "Incomum";
            case RARE -> "Rara";
            case EPIC -> "Épica";
            case LEGENDARY -> "Lendária";
        };
    }

}
