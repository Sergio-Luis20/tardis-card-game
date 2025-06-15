package br.sergio.tcg.discord.slash.commands;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.slash.SlashCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CloseMatchCommand extends SlashCommand {

    public CloseMatchCommand() {
        super("closematch", "Encerra a partida atual, se você for o host");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event, Member member) {
        var sessionManager = DiscordService.getInstance().getSessionManager();
        var opt = sessionManager.findSession(member);
        if (opt.isEmpty()) {
            event.reply("Você não está em nenhuma partida no momento.").setEphemeral(true).queue();
            return;
        }
        var session = opt.get();
        if (!session.getHost().getMember().equals(member)) {
            event.reply("Você não é o dono da partida.").setEphemeral(true).queue();
            return;
        }
        sessionManager.closeSession(session);
        event.reply("Partida fechada: " + session.getId()).queue();
    }

}
