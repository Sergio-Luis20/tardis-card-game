package br.sergio.tcg.game.card.attack;

import br.sergio.tcg.Utils;
import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.AttributeInstance;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.AttackCard;
import br.sergio.tcg.game.card.Card;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.card.family.ClownGirlCard;
import br.sergio.tcg.game.effect.DoTEffect;
import br.sergio.tcg.game.query.queries.SelectOneQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ClownGirlFurina extends AttackCard implements ClownGirlCard {

    public ClownGirlFurina() {
        super("Garota palhaço Furina", Rarity.EPIC, "Ao ser ativada, esta carta permanece " +
                "em campo. Uma vez por turno o oponente deve ou não revelar uma carta. Para cada vez que " +
                "ele revelar uma carta, adicione 1 ao contador da verdade, e para cada vez que ele não " +
                "revelar, adicione 1 ao contador da mentira. A cada 2 pontos de verdade, compre uma carta; " +
                "a cada 2 pontos de mentira, o oponente perde 50 pontos de vida.", "https://i.imgur.com/cDQ7oqF.png");
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        var attacker = turn.getAttacker();
        var defender = turn.getDefender();
        var effect = new ClownGirlFurinaEffect(turn.getSession(), attacker, defender);
        turn.getBattleDetails().addEffect(defender, effect);
        turn.logf("%s aplicou o efeito da garota palhaço Furina em %s!", attacker.getName(), defender.getName());
        return completedAction();
    }

    public static class ClownGirlFurinaEffect implements DoTEffect {

        private GameSession session;
        private Player attacker, defender;
        private AtomicInteger truth, lie;
        private List<Card> revealedCards;
        private AttributeInstance damage;

        public ClownGirlFurinaEffect(GameSession session, Player attacker, Player defender) {
            Utils.nonNull(session, attacker, defender);
            this.session = session;
            this.attacker = attacker;
            this.defender = defender;
            this.revealedCards = new ArrayList<>();
            this.truth = new AtomicInteger();
            this.lie = new AtomicInteger();
            this.damage = new AttributeInstance(50);
        }

        @Override
        public void tick() {
            var cards = new ArrayList<>(attacker.getHand());
            cards.removeAll(revealedCards);
            var query = new SelectOneQuery<>(
                    defender,
                    "Escolha uma carta para revelar",
                    cards,
                    true,
                    true,
                    card -> DiscordService.getInstance().getEmbedFactory().createCardEmbed(attacker, card)
            );
            session.query(query, (q, card) -> {
                if (card != null) {
                    revealedCards.add(card);
                    session.revealCards(defender, List.of(card));
                    if (truth.incrementAndGet() % 2 == 0) {
                        if (session.draw(attacker) != null) {
                            session.logf("%s comprou uma carta pelo contador da verdade!", attacker.getBoldName());
                        } else {
                            session.logf("%s tentou comprar uma carta pelo contador da verdade, mas " +
                                    "o deck estava vazio!", attacker.getBoldName());
                        }
                    }
                } else {
                    if (lie.incrementAndGet() % 2 == 0) {
                        int damageValue = (int) damage.calculate();
                        defender.subtractHp(damageValue);
                        session.logf("%s tomou **%d** de dano pelo contador da mentira!",
                                defender.getBoldName(), damageValue);
                    }
                }
            });
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public AttributeInstance getAttribute() {
            return damage;
        }

    }

}
