package br.sergio.tcg.game.card;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.*;

@ToString
@EqualsAndHashCode
public class CardCombination implements Iterable<CombinableCard>, Cloneable {

    private List<CombinableCard> cards;

    public CardCombination() {
        cards = new ArrayList<>();
    }

    public CardCombination(CombinableCard... cards) {
        this(Arrays.asList(cards));
    }

    public CardCombination(Collection<? extends CombinableCard> cards) {
        this.cards = new ArrayList<>(cards);
    }

    public CardCombination(CardCombination other) {
        cards = new ArrayList<>(other.cards);
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

    public List<Card> getCards() {
        return cards.stream().map(Card.class::cast).toList();
    }

    public List<CombinableCard> getCombinableCards() {
        return Collections.unmodifiableList(cards);
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
