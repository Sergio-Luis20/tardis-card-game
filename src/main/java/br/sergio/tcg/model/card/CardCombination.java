package br.sergio.tcg.model.card;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

@ToString
@EqualsAndHashCode
public class CardCombination implements Iterable<CombinableCard>, Cloneable {

    private Set<CombinableCard> cards;

    public CardCombination() {
        cards = new HashSet<>();
    }

    public CardCombination(CombinableCard... cards) {
        this.cards = new HashSet<>(Arrays.asList(cards));
    }

    public CardCombination(CardCombination other) {
        cards = new HashSet<>(other.cards);
    }

    public CardCombination add(CombinableCard combinableCard) {
        cards.add(combinableCard);
        return this;
    }

    public CardCombination remove(CombinableCard combinableCard) {
        cards.remove(combinableCard);
        return this;
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public boolean contains(CombinableCard card) {
        return cards.contains(card);
    }

    public CardCombination combine(CardCombination other) {
        var newCombination = new CardCombination();
        newCombination.cards.addAll(cards);
        newCombination.cards.addAll(other.cards);
        return newCombination;
    }

    public void trigger() {
        for (var combinableCard : cards) {
            combinableCard.whenCombined(this);
        }
    }

    public Set<Card> getCards() {
        return cards.stream().map(Card.class::cast).collect(Collectors.toSet());
    }

    public Set<CombinableCard> getCombinableCards() {
        return Collections.unmodifiableSet(cards);
    }

    @Override
    public Iterator<CombinableCard> iterator() {
        return cards.iterator();
    }

    @Override
    public CardCombination clone() {
        return new CardCombination(this);
    }

}
