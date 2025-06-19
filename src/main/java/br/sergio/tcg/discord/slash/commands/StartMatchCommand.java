package br.sergio.tcg.discord.slash.commands;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.slash.SlashCommand;
import br.sergio.tcg.game.GameSession;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class StartMatchCommand extends SlashCommand {

    public StartMatchCommand() {
        super("startmatch", "Começa a partida, se você for o host.");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event, Member member) {
        var opt = DiscordService.getInstance().getSessionManager().ofHost(member);
        if (opt.isEmpty()) {
            event.reply("Você não é host de nenhuma partida.").setEphemeral(true).queue();
            return;
        }
        var session = opt.get();
        if (session.hasStarted()) {
            event.reply("Sua partida já começou.").setEphemeral(true).queue();
            return;
        }
        if (session.getPlayers().size() < GameSession.MIN_PLAYERS) {
            event.reply("A quantidade de jogadores deve ser no mínimo " + GameSession.MIN_PLAYERS + ". Jogadores atualmente na partida: " + session.getPlayers().size() + ".").setEphemeral(true).queue();
            return;
        }
        session.start();
        event.reply("A partida de **" + session.getHost().getName() + "** (" + session.getId() + ") começou!").queue();
    }

}
