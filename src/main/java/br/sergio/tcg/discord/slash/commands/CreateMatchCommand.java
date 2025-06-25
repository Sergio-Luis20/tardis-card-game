package br.sergio.tcg.discord.slash.commands;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.slash.Option;
import br.sergio.tcg.discord.slash.SlashCommand;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.IllegalSessionCreationException;
import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.SessionManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;

public class CreateMatchCommand extends SlashCommand {

    public CreateMatchCommand() {
        super("creatematch", "Cria uma nova partida de TCG");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event, Member member) {
        var player = new Player(member);
        try {
            int minPlayers = event.getOption("min-players", GameSession.MIN_PLAYERS, OptionMapping::getAsInt);
            int maxPlayers = event.getOption("max-players", GameSession.MAX_PLAYERS, OptionMapping::getAsInt);
            var service = DiscordService.getInstance();
            var gameChannel = service.getGuild().equals(event.getGuild()) ? service.getGameChannel() : event.getChannel();
            var session = DiscordService.getInstance().getSessionManager().createSession(player, gameChannel, minPlayers, maxPlayers);
            event.reply("Partida criada: " + session.getId() + ". Ela deve iniciar em no máximo "
                    + SessionManager.TIME_TO_JOIN + " segundos para evitar que seja deletada " +
                    "automaticamente.").queue();
        } catch (IllegalSessionCreationException e) {
            event.reply("Você não pode criar uma partida estando numa. Saia da atual primeiro.\n" + e).queue();
        }
    }

    @Override
    public List<Option> options() {
        Option minPlayers = new Option(OptionType.INTEGER, "min-players", "Quantidade mínima de jogadores", false, null);
        Option maxPlayers = new Option(OptionType.INTEGER, "max-payers", "Quantidade máxima de jogadores", false, null);
        return List.of(minPlayers, maxPlayers);
    }

}
