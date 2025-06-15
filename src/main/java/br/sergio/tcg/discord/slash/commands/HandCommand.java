package br.sergio.tcg.discord.slash.commands;

import br.sergio.tcg.discord.CardEmbed;
import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.slash.SlashCommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;

@Slf4j
public class HandCommand extends SlashCommand {

    public HandCommand() {
        super("hand", "Mostra suas cartas");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event, Member member) {
        var service = DiscordService.getInstance();
        var opt = service.getSessionManager().findSession(member);
        if (opt.isEmpty()) {
            event.reply("Você não está numa partida.").setEphemeral(true).queue();
            return;
        }
        var session = opt.get();
        var player = session.findPlayer(member);
        var hand = player.getHand();
        var embedFactory = service.getEmbedFactory();
        var cardEmbeds = new ArrayList<CardEmbed>(hand.size());
        for (var card : hand) {
            cardEmbeds.add(embedFactory.createCardEmbed(player, card));
        }
        member.getUser().openPrivateChannel().queue(channel -> CardEmbed.sendAll(channel, cardEmbeds), t -> {
            log.error("Failed to get private channel of {}", member.getEffectiveName(), t);
        });
    }

}
