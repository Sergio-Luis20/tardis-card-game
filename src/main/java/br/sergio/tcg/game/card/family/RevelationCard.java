package br.sergio.tcg.game.card.family;

public interface RevelationCard extends CardFamily {

    default String getFamilyName() {
        return "Revelação";
    }

}
