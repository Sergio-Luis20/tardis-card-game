package br.sergio.tcg.model.card;

public interface CombinableCard {

    default CardCombination combine(CombinableCard other) {
        var combination = new CardCombination(this, other);
        combination.trigger();
        return combination;
    }

    default CardCombination combine(CardCombination combination) {
        combination.add(this).trigger();
        return combination;
    }

    default void whenCombined(CardCombination combination) {
        // Called when a CardCombination is triggered.
    }

}
