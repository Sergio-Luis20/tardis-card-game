package br.sergio.tcg.game.battle;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.AttributeInstance;
import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.AttackCard;
import br.sergio.tcg.game.card.DefaultDefenseCard;
import br.sergio.tcg.game.card.DefenseCard;
import br.sergio.tcg.game.effect.StatusEffect;
import br.sergio.tcg.game.event.events.PlayerDamageEvent;
import br.sergio.tcg.game.query.queries.SelectOneQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

@Slf4j
@Getter
public class BattleDetails {

    private TurnDetails turn;
    private Player attacker, defender;

    @Setter
    private volatile AttackCard attackCard;

    @Setter
    private volatile DefenseCard defenseCard;

    private volatile int attackerRoll, defenderRoll;
    private CompletableFuture<Void> attackerRollFuture, defenderRollFuture;
    private volatile boolean rollDicePhase;

    private List<BattleTask> battleTasks = Collections.synchronizedList(new ArrayList<>());
    private Map<Player, List<StatusEffect>> effects = new ConcurrentHashMap<>();
    private Map<Player, List<AttributeInstance>> hpVariations = new ConcurrentHashMap<>();

    private AtomicBoolean resolved = new AtomicBoolean();

    public BattleDetails(TurnDetails turn, Player attacker, Player defender) {
        this.turn = requireNonNull(turn, "turn");
        this.attacker = requireNonNull(attacker, "attacker");
        this.defender = requireNonNull(defender, "defender");
        this.attackerRollFuture = new CompletableFuture<>();
        this.defenderRollFuture = new CompletableFuture<>();
    }

    public void doBattle() {
        // 1. Escolher cartas

        var attackCardFuture = new CompletableFuture<AttackCard>();
        var defenseCardFuture = new CompletableFuture<DefenseCard>();
        var attackCards = attacker.getHand().stream()
                .filter(AttackCard.class::isInstance)
                .map(AttackCard.class::cast)
                .toList();
        var attackCardQuery = new SelectOneQuery<>(
                attacker,
                "Escolha uma carta para atacar " + defender.getBoldName() + ".",
                attackCards,
                true,
                false,
                card -> DiscordService.getInstance().getEmbedFactory().createCardEmbed(attacker, card)
        );
        var session = turn.getSession();
        session.query(attackCardQuery, (q, card) -> attackCardFuture.complete(card));
        var defenseCards = defender.getHand().stream()
                .filter(DefenseCard.class::isInstance)
                .map(DefenseCard.class::cast)
                .toList();
        if (defenseCards.isEmpty()) {
            defenseCardFuture.complete(DefaultDefenseCard.INSTANCE);
        } else {
            var defenseCardQuery = new SelectOneQuery<>(
                    defender,
                    "Deseja usar uma carta de defesa contra " + attacker.getBoldName() + "?",
                    defenseCards,
                    true,
                    true,
                    card -> DiscordService.getInstance().getEmbedFactory().createCardEmbed(defender, card)
            );
            session.query(defenseCardQuery, (q, card) -> defenseCardFuture.complete(card == null ? DefaultDefenseCard.INSTANCE : card));
        }

        try {
            attackCard = attackCardFuture.get();
            defenseCard = defenseCardFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to get attack card", e);
            return;
        }

        session.showCard(attacker, attackCard);
        session.showCard(defender, defenseCard);

        // 2. Rolar dados

        rollDicePhase = true;
        turn.log("Hora de rolar dados!");
        CompletableFuture.allOf(attackerRollFuture, defenderRollFuture).join();
        rollDicePhase = false;

        // 3. Executar cartas

        if (attackerRoll < defenderRoll || attackerRoll == defenderRoll && defenseCard != DefaultDefenseCard.INSTANCE) {
            turn.logf("%s venceu na rolagem de dados!", defender.getBoldName());
            attackCard.action(turn).join();
            defenseCard.action(turn).join();
        } else {
            turn.logf("%s venceu na rolagem de dados!", attacker.getBoldName());
            attackCard.action(turn).join();
        }

        // 4. Chamar de volta turn.effectPhaseCard

        if (attacker.isDead()) {
            turn.logf("%s morreu!", attacker.getBoldName());
            return;
        }

        turn.effectCardPhase();
    }

    public void setAttackerRoll(int roll) {
        assert roll >= 1 && roll <= 6;
        attackerRoll = roll;
        attackerRollFuture.complete(null);
    }

    public void setDefenderRoll(int roll) {
        assert roll >= 1 && roll <= 6;
        defenderRoll = roll;
        defenderRollFuture.complete(null);
    }

    public void addBattleTask(BattleTask task) {
        battleTasks.add(task);
    }

    public void removeBattleTask(BattleTask task) {
        battleTasks.remove(task);
    }

    public void addEffect(Player target, StatusEffect effect) {
        getEffects(target).add(effect);
    }

    public void removeEffect(Player target, StatusEffect effect) {
        getEffects(target).remove(effect);
    }

    public List<StatusEffect> getEffects(Player target) {
        return effects.computeIfAbsent(target, p -> Collections.synchronizedList(new ArrayList<>()));
    }

    public void addHpVariation(Player target, AttributeInstance hpVariation) {
        getHpVariations(target).add(hpVariation);
    }

    public void removeHpVariation(Player target, AttributeInstance hpVariation) {
        getHpVariations(target).remove(hpVariation);
    }

    public List<AttributeInstance> getHpVariations(Player target) {
        return hpVariations.computeIfAbsent(target, p -> Collections.synchronizedList(new ArrayList<>()));
    }

    public void clearAll() {
        battleTasks.clear();
        effects.values().forEach(List::clear);
        hpVariations.values().forEach(List::clear);
    }

    public synchronized void resolve() {
        if (resolved.get()) return;

        var futures = battleTasks.stream()
                .map(BattleTask::action)
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).exceptionally(t -> {
            log.error("Exception on battle task", t);
            return null;
        }).join();

        for (var entry : effects.entrySet()) {
            var target = entry.getKey();
            for (var effect : entry.getValue()) {
                target.addEffect(effect);
            }
        }

        for (var entry : hpVariations.entrySet()) {
            var target = entry.getKey();
            for (var variation : entry.getValue()) {
                if (variation.calculate() < 0) {
                    var damageEvent = new PlayerDamageEvent(attacker, target, variation);
                    turn.getSession().getEventRegistry().callEvent(damageEvent);
                    target.addHp((int) damageEvent.getDamage().calculate());
                } else {
                    target.addHp((int) variation.calculate());
                }
            }
        }

        resolved.set(true);
    }

}
