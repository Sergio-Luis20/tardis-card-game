package br.sergio.tcg.discord.slash.commands;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.slash.SlashCommand;
import br.sergio.tcg.game.IllegalSessionCreationException;
import br.sergio.tcg.model.Player;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CreateMatchCommand extends SlashCommand {

    public CreateMatchCommand() {
        super("creatematch", "Cria uma nova partida de TCG");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event, Member member) {
        var player = new Player(member);
        try {
            var session = DiscordService.getInstance().getSessionManager().createSession(player);
            event.reply("Partida criada: " + session.getId()).queue();
        } catch (IllegalSessionCreationException e) {
            event.reply("Você não pode criar uma partida estando numa. Saia primeiro da atual.\n" + e).queue();
        }
    }

}
