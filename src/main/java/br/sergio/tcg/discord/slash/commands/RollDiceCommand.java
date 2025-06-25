package br.sergio.tcg.discord.slash.commands;

import br.sergio.tcg.Utils;
import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.slash.SlashCommand;
import br.sergio.tcg.game.DiceOrdering;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.Player;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

@Slf4j
public class RollDiceCommand extends SlashCommand {

    public RollDiceCommand() {
        super("rolldice", "Rola o dado");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event, Member member) {
        var opt = DiscordService.getInstance().getSessionManager().findSession(member);
        if (opt.isEmpty()) {
            event.reply("Você não está em partida.").setEphemeral(true).queue();
            return;
        }
        var session = opt.get();
        if (!session.hasStarted()) {
            event.reply("A partida ainda não começou.").setEphemeral(true).queue();
            return;
        }
        var diceOrdering = session.getDiceOrdering();
        var player = session.findByMember(member).orElseThrow();
        if (!diceOrdering.allOrdersDefined()) {
            onOrdering(session, diceOrdering, player, event);
        } else {
            onNormalRoll(session, player, event);
        }
    }

    private void onOrdering(GameSession session, DiceOrdering diceOrdering,
                            Player player, SlashCommandInteractionEvent event) {
        if (diceOrdering.getOrderedList().contains(player)) {
            event.reply("Você já tem sua posição definida.").setEphemeral(true).queue();
            return;
        }
        if (diceOrdering.getCurrentRolls().containsKey(player)) {
            event.reply("Você já rolou dados nesta rodada.").setEphemeral(true).queue();
            return;
        }

        int roll = Utils.rollDice();
        if (!diceOrdering.submitRoll(player, roll)) {
            event.reply("Você já tem posição definida ou já rolou o dado nesta rodada.").setEphemeral(true).queue();
            return;
        }

        var service = DiscordService.getInstance();
        var selfUser = service.getJda().getSelfUser();
        var embed = service.getEmbedFactory().createRollEmbed(player, roll);

        event.replyEmbeds(embed.embed()).queue(success -> {
            var ch = event.getChannel();

            if (!diceOrdering.isRoundReady()) {
                return;
            }
            if (!diceOrdering.resolveRound()) {
                ch.sendMessageEmbeds(
                        new EmbedBuilder()
                                .setAuthor(selfUser.getEffectiveName(), null, selfUser.getEffectiveAvatarUrl())
                                .setTitle("⚠️ Rodada empatada!")
                                .setDescription("Empate no maior valor. Todos os jogadores restantes devem rolar novamente")
                                .setColor(Color.YELLOW)
                                .setTimestamp(Utils.dateTime())
                                .build()
                ).queue();
                return;
            }

            if (diceOrdering.allOrdersDefined()) {
                var orderedList = diceOrdering.getOrderedList();
                var order = orderedList.stream()
                        .map(Player::getName)
                        .reduce((a, b) -> a + " → " + b)
                        .orElse("");
                ch.sendMessageEmbeds(
                        getDefinedPosition(selfUser, orderedList.get(orderedList.size() - 2), orderedList.size() - 1),
                        getDefinedPosition(selfUser, orderedList.getLast(), orderedList.size()),
                        new EmbedBuilder()
                                .setAuthor(selfUser.getEffectiveName(), null, selfUser.getEffectiveAvatarUrl())
                                .setTitle("\uD83C\uDFC6 Ordem final definida!")
                                .setDescription(order)
                                .setColor(Color.CYAN)
                                .setTimestamp(Utils.dateTime())
                                .build()
                ).queue();
                session.ordersDefined();
            } else {
                var winner = diceOrdering.getOrderedList().getLast();
                int pos = diceOrdering.getOrderedList().indexOf(winner) + 1;
                var ordered = Utils.formatCollection(diceOrdering.getOrderedList().stream().map(Player::getName).toList());
                var remaining = Utils.formatCollection(diceOrdering.getRemainingPlayers().stream().map(Player::getName).toList());
                ch.sendMessageEmbeds(
                        getDefinedPosition(selfUser, winner, pos),
                        new EmbedBuilder()
                                .setAuthor(selfUser.getEffectiveName(), null, selfUser.getEffectiveAvatarUrl())
                                .setTitle("✅ Rodada concluída!")
                                .setColor(Color.GREEN)
                                .addField("Posições garantidas", ordered, false)
                                .addField("Jogadores que ainda faltam", remaining, false)
                                .setTimestamp(Utils.dateTime())
                                .build()
                ).queue();
            }
        }, t -> log.info("Failed to reply a rolldice command", t));
    }

    private void onNormalRoll(GameSession session, Player player, SlashCommandInteractionEvent event) {
        // Aqui é para quando os jogadores rolam dados durante o jogo
        var turn = session.getCurrentTurn();
        if (turn.isPacific()) {
            event.reply("O turno não é de batalha.").setEphemeral(true).queue();
        } else if (turn.getBattleDetails().isRollDicePhase()) {
            event.reply("Não está na hora de rolar dados.").setEphemeral(true).queue();
        } else if (turn.getAttacker().equals(player)) {
            var battle = turn.getBattleDetails();
            if (battle.getAttackerRoll() != 0) {
                event.reply("Você já rolou o dado.").setEphemeral(true).queue();
            } else {
                int roll = Utils.rollDice();
                var rollEmbed = DiscordService.getInstance().getEmbedFactory().createRollEmbed(player, roll);
                event.replyEmbeds(rollEmbed.embed()).queue();
                battle.setAttackerRoll(roll);
            }
        } else if (turn.getDefender().equals(player)) {
            var battle = turn.getBattleDetails();
            if (battle.getDefenderRoll() != 0) {
                event.reply("Você já rolou o dado.").setEphemeral(true).queue();
            } else {
                int roll = Utils.rollDice();
                var rollEmbed = DiscordService.getInstance().getEmbedFactory().createRollEmbed(player, roll);
                event.replyEmbeds(rollEmbed.embed()).queue();
                battle.setDefenderRoll(roll);
            }
        } else {
            event.reply("Você não participa deste turno.").setEphemeral(true).queue();
        }
    }

    private MessageEmbed getDefinedPosition(SelfUser selfUser, Player winner, int pos) {
        return new EmbedBuilder()
                .setAuthor(selfUser.getEffectiveName(), null, selfUser.getEffectiveAvatarUrl())
                .setTitle("» Posição definida!")
                .setDescription(winner.getName() + " garantiu sua posição na ordem como " + pos + "º colocado")
                .setColor(Color.ORANGE)
                .setTimestamp(Utils.dateTime())
                .build();
    }

}
