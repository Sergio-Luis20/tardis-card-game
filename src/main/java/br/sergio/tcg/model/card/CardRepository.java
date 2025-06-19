package br.sergio.tcg.model.card;

import br.sergio.tcg.model.card.family.CardFamily;
import lombok.Getter;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class CardRepository {

    @Getter
    private static final CardRepository instance = new CardRepository();

    private Map<Class<?>, Card> cards;

    private CardRepository() {
        cards = new ConcurrentHashMap<>();
        for (var card : ServiceLoader.load(Card.class)) {
            cards.put(card.getClass(), card);
        }
    }

    public boolean existsByClass(Class<?> id) {
        return cards.containsKey(id);
    }

    public <T> T findByClass(Class<T> cardClass) {
        if (!Card.class.isAssignableFrom(cardClass)) {
            throw new IllegalArgumentException("Not a card class: " + cardClass.getName());
        }
        if (Modifier.isAbstract(cardClass.getModifiers())) {
            throw new IllegalArgumentException("No a concrete class: " + cardClass.getName());
        }
        return cards.values().stream()
                .filter(card -> card.getClass() == cardClass)
                .findFirst()
                .map(cardClass::cast)
                .orElseThrow();
    }

    public Map<Class<?>, Card> getMap() {
        return Collections.unmodifiableMap(cards);
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards.values());
    }

    public List<AttackCard> getAttackCards() {
        return findByType(AttackCard.class);
    }

    public List<DefenseCard> getDefenseCards() {
        return findByType(DefenseCard.class);
    }

    public List<EffectCard> getEffectCards() {
        return findByType(EffectCard.class);
    }

    public List<Card> findByFamily(Class<? extends CardFamily> familyInterface) {
        if (!familyInterface.isInterface()) {
            throw new IllegalArgumentException("Not a family interface: " + familyInterface.getName());
        }
        return cards.values().stream()
                .filter(familyInterface::isInstance)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private <T> List<T> findByType(Class<T> cardClass) {
        return cards.values().stream()
                .filter(cardClass::isInstance)
                .map(cardClass::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
