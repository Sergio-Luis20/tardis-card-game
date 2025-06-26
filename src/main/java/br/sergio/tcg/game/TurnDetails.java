package br.sergio.tcg.game;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.battle.BattleDetails;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.query.queries.SelectManyQuery;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TurnDetails {

    @Include
    private UUID turnId;
    private GameSession session;
    private boolean pacific;
    private Player player, attacker, defender;
    private volatile boolean turnStarted, turnTerminated;
    private BattleDetails battleDetails;
    private List<Runnable> turnStartTasks, turnEndTasks;

    private TurnDetails() {
        turnId = UUID.randomUUID();
        turnStartTasks = Collections.synchronizedList(new ArrayList<>());
        turnEndTasks = Collections.synchronizedList(new ArrayList<>());
    }

    public synchronized void startTurn() {
        if (turnStarted) {
            return;
        }
        turnStartTasks.forEach(Runnable::run);

        if (pacific) {
            session.draw(player);
            logf("%s comprou uma carta!", player.getBoldName());
            effectCardPhase();
        } else {
            Thread.startVirtualThread(battleDetails::doBattle);
        }

        turnStarted = true;
    }

    public synchronized void endTurn() {
        if (turnTerminated) {
            return;
        }
        turnEndTasks.forEach(Runnable::run);
        turnTerminated = true;

        logf("Turno de %s terminado", player.getBoldName());
        session.nextTurn();
        session.startTurn();
    }

    public synchronized void effectCardPhase() {
        Thread.startVirtualThread(() -> {
            var effectCards = player.getHand().stream()
                    .filter(EffectCard.class::isInstance)
                    .map(EffectCard.class::cast)
                    .toList();
            if (effectCards.isEmpty()) {
                logf("%s não possui nenhuma carta de efeito para usar.", player.getBoldName());
                endTurn();
                return;
            }
            logf("%s está decidindo se usará cartas de efeito.", player.getBoldName());
            var query = new SelectManyQuery<>(
                    player,
                    "Deseja usar alguma carta de efeito?",
                    effectCards,
                    true,
                    true,
                    card -> DiscordService.getInstance().getEmbedFactory().createCardEmbed(player, card)
            );
            session.query(query, (q, cards) -> {
                if (cards.isEmpty()) {
                    logf("%s escolheu não usar cartas de efeito.", player.getBoldName());
                    endTurn();
                    return;
                }
                for (var effectCard : cards) {
                    logf("%s usou a carta de efeito \"%s\"", player.getBoldName(), effectCard.getName());
                    effectCard.action(this).join();
                    if (player.getHand().contains(effectCard)) {
                        session.sendCardToDeck(player, effectCard);
                    }
                }
                endTurn();
            });
        });
    }

    public void resetBattle(boolean printDefenseMessage) {
        if (!pacific) {
            battleDetails.clearAll();
            if (printDefenseMessage) {
                logf("Os efeitos da carta de ataque **%s** foram anulados!", battleDetails.getAttackCard().getName());
            }
        }
    }

    public void log(String message) {
        session.log(message);
    }

    public void logf(String format, Object... args) {
        session.logf(format, args);
    }

    public static TurnDetails pacific(GameSession session, Player player) {
        var details = new TurnDetails();
        details.session = session;
        details.player = player;
        details.pacific = true;
        return details;
    }

    public static TurnDetails battle(GameSession session, Player attacker, Player defender) {
        var details = new TurnDetails();
        details.session = session;
        details.player = attacker;
        details.attacker = attacker;
        details.defender = defender;
        details.battleDetails = new BattleDetails(details, attacker, defender);
        return details;
    }

}
