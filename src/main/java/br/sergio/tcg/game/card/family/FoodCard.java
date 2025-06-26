package br.sergio.tcg.game.card.family;

public interface FoodCard extends CardFamily {

    default String getFamilyName() {
        return "Carta de comida";
    }

}
