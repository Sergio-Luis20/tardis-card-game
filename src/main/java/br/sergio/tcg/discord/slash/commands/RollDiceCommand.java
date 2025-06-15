package br.sergio.tcg.discord.slash.commands;

import br.sergio.tcg.Utils;
import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.slash.SlashCommand;
import br.sergio.tcg.game.DiceOrdering;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

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
        var player = session.findPlayer(member);
        if (!diceOrdering.allOrdersDefined()) {
            onOrdering(session, diceOrdering, player, event);
        } else {
            onNormalRoll(session, player, event);
        }
    }

    private void onOrdering(GameSession session, DiceOrdering diceOrdering, Player player, SlashCommandInteractionEvent event) {
        if (diceOrdering.getOrderedList().contains(player)) {
            event.reply("Você já tem sua posição definida.").setEphemeral(true).queue();
            return;
        }
        if (diceOrdering.getCurrentRolls().containsKey(player)) {
            event.reply("Você já rolou dados nesta rodada.").setEphemeral(true).queue();
            return;
        }

        int roll = Utils.rollDice();
        diceOrdering.submitRoll(player, roll);

        var service = DiscordService.getInstance();
        var selfUser = service.getJda().getSelfUser();
        var embed = service.getEmbedFactory().createRollEmbed(player, roll);

        event.replyEmbeds(embed).queue();

        if (diceOrdering.isRoundReady()) {
            var winner = diceOrdering.getOrderedList().getLast();
            int pos = diceOrdering.getOrderedList().indexOf(winner) + 1;
            event.getChannel().sendMessageEmbeds(
                    new EmbedBuilder()
                            .setAuthor(selfUser.getEffectiveName(), null, selfUser.getEffectiveAvatarUrl())
                            .setTitle("✅ Rodada concluída!")
                            .setDescription(winner.getName() + " garantiu sua posição na ordem como " + pos + "º colocado")
                            .setColor(Color.GREEN)
                            .setTimestamp(Utils.dateTime())
                            .build()
            ).queue();
        } else {
            event.getChannel().sendMessageEmbeds(
                    new EmbedBuilder()
                            .setAuthor(selfUser.getEffectiveName(), null, selfUser.getEffectiveAvatarUrl())
                            .setTitle("⚠️ Rodada empatada!")
                            .setDescription("Empate no maior valor. Todos os jogadores restantes devem rolar novamente")
                            .setColor(Color.YELLOW)
                            .setTimestamp(Utils.dateTime())
                            .build()
            ).queue();
        }

        if (diceOrdering.allOrdersDefined()) {
            session.ordersDefined();

            var order = diceOrdering.getOrderedList().stream()
                    .map(Player::getName)
                    .reduce((a, b) -> a + " → " + b)
                    .orElse("");

            event.getChannel().sendMessageEmbeds(
                    new EmbedBuilder()
                            .setAuthor(selfUser.getEffectiveName(), null, selfUser.getEffectiveAvatarUrl())
                            .setTitle("\uD83C\uDFC6 Ordem final definida!")
                            .setDescription(order)
                            .setColor(Color.CYAN)
                            .setTimestamp(Utils.dateTime())
                            .build()
            ).queue();
        }
    }

    private void onNormalRoll(GameSession session, Player player, SlashCommandInteractionEvent event) {
        // Aqui é para quando os jogadores rolam dados durante o jogo
    }

}
