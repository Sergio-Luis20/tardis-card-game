package br.sergio.tcg.discord.slash.commands;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.slash.SlashCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class DrawCommand extends SlashCommand {

    public DrawCommand() {
        super("drawcard", "Compra uma carta");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event, Member member) {
        var opt = DiscordService.getInstance().getSessionManager().findSession(member);
        if (opt.isEmpty()) {
            event.reply("Você não está em jogo.").setEphemeral(true).queue();
            return;
        }
        var session = opt.get();
        if (!session.hasStarted()) {
            event.reply("A partida ainda não começou.").setEphemeral(true).queue();
            return;
        }
        consumeInteraction(event);
        var player = session.findByMember(member).orElseThrow();
        if (session.draw(player)) {
            session.logf("%s comprou uma carta!", player.getBoldName());
        } else {
            session.logf("%s falhou ao tentar comprar uma carta porque o deck está vazio!", player.getBoldName());
        }
    }

}
