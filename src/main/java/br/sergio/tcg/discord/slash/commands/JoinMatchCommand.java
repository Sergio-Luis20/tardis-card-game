package br.sergio.tcg.discord.slash.commands;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.slash.Option;
import br.sergio.tcg.discord.slash.SlashCommand;
import br.sergio.tcg.game.Player;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;

public class JoinMatchCommand extends SlashCommand {

    public JoinMatchCommand() {
        super("joinmatch", "Entra numa partida criada por outra pessoa.");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event, Member member) {
        var host = event.getOption("host", OptionMapping::getAsMember);
        var sessionManager = DiscordService.getInstance().getSessionManager();
        if (sessionManager.findSession(member).isPresent()) {
            event.reply("Você já está numa partida. Saia da atual primeiro para poder entrar em outra.").setEphemeral(true).queue();
            return;
        }
        var opt = sessionManager.ofHost(host);
        if (opt.isEmpty()) {
            event.reply("O usuário " + host.getEffectiveName() + " não é host de nenhuma partida atualmente.").setEphemeral(true).queue();
            return;
        }
        var session = opt.get();
        if (session.hasStarted()) {
            event.reply("A partida de " + host.getEffectiveName() + " já começou.").setEphemeral(true).queue();
            return;
        }
        if (session.getPlayers().size() == session.getMaxPlayers()) {
            event.reply("A partida de " + host.getEffectiveName() + " está cheia.").setEphemeral(true).queue();
            return;
        }
        consumeInteraction(event);
        var newPlayer = new Player(member);
        session.addPlayer(newPlayer);
        session.logf("» %s entrou na partida de %s (%d/%d)!", newPlayer.getBoldName(), session.getHost().getBoldName(), session.getPlayers().size(), session.getMaxPlayers());
    }

    @Override
    public List<Option> options() {
        return List.of(new Option(OptionType.USER, "host", "O usuário host da partida."));
    }

}
