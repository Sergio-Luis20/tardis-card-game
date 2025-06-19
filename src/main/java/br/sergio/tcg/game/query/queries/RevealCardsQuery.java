package br.sergio.tcg.game.query.queries;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.event.events.PlayerRevealCardEvent;
import br.sergio.tcg.game.query.Query;
import br.sergio.tcg.game.query.QueryManager;
import br.sergio.tcg.model.Player;
import br.sergio.tcg.model.card.Card;

import java.util.*;

public record RevealCardsQuery(Player revealer, Set<Player> targets) implements Query<List<Card>> {

    public RevealCardsQuery(Player revealer, Set<Player> targets) {
        Utils.nonNull(revealer);
        Utils.nonNull(targets);
        this.revealer = revealer;
        this.targets = Collections.unmodifiableSet(targets);
    }

    public RevealCardsQuery(Player revealer, Collection<Player> targets) {
        this(revealer, new HashSet<>(targets));
    }

    public RevealCardsQuery(Player revealer, Player... targets) {
        this(revealer, Set.of(targets));
    }

    @Override
    public Player target() {
        return revealer;
    }

    @Override
    public Map<String, Object> attributes() {
        return Map.of("revealer", revealer, "targets", this.targets);
    }

    @Override
    public <P extends Player> void execute(QueryManager queryManager, GameSession<P> session) {
        session.getDriver().chooseMany(revealer, "Selecione quaisquer cartas.", revealer.getHand(), Card.class, true).thenAccept(result -> {
            var event = new PlayerRevealCardEvent(revealer, result, targets);
            session.getEventRegistry().callEvent(event);
            queryManager.complete(this, result);
        });
    }

}
