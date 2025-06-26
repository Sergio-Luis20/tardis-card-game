package br.sergio.tcg.discord.slash.commands;

import br.sergio.tcg.discord.CardScrollListener;
import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.slash.SlashCommand;
import br.sergio.tcg.game.Player;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HandCommand extends SlashCommand {

    private final Map<Player, CardScrollListener> handListeners = new ConcurrentHashMap<>();

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
        var player = session.findByMember(member).orElseThrow();
        var oldListener = handListeners.remove(player);
        if (oldListener != null) {
            oldListener.cleanup();
        }
        var hand = new ArrayList<>(player.getHand());
        if (hand.isEmpty()) {
            event.reply("Você está atualmente sem cartas.").setEphemeral(true).queue();
            return;
        }
        consumeInteraction(event);
        member.getUser().openPrivateChannel().queue(pv -> {
            var firstEmbed = service.getEmbedFactory().createCardEmbed(player, hand.getFirst());
            var interactionId = UUID.randomUUID().toString();
            pv.sendMessage("Suas cartas:")
                    .setEmbeds(firstEmbed)
                    .setActionRow(CardScrollListener.makeButtons(interactionId, hand))
                    .queue(handMessage -> {
                var jda = service.getJda();
                var listener = new CardScrollListener(interactionId, player, handMessage, jda, hand, handListeners);
                handListeners.put(player, listener);
            }, t -> {
                log.error("Failed to send hand message to {}", player.getName());
                session.logf("Não consigo te mandar mensagens no privado, %s.", member.getAsMention());
            });
        }, t -> {
            log.error("Failed to get private channel of {}", player.getName());
            session.logf("Não acessar seu privado, %s.", member.getAsMention());
        });
    }

}
