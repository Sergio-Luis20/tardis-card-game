package br.sergio.tcg.discord.slash.commands;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.slash.SlashCommand;
import br.sergio.tcg.game.card.CardCombination;
import br.sergio.tcg.game.card.CombinableCard;
import br.sergio.tcg.game.query.queries.SelectManyQuery;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CombineCommand extends SlashCommand {

    public CombineCommand() {
        super("combine", "Combina duas ou mais cartas combináveis.");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event, Member member) {
        var opt = DiscordService.getInstance().getSessionManager().findSession(member);
        if (opt.isEmpty()) {
            event.reply("Você não está em jogo.").setEphemeral(true).queue();
            return;
        }
        var session = opt.get();
        var player = session.findByMember(member).orElseThrow();
        var combinableCards = player.getHand().stream()
                .filter(CombinableCard.class::isInstance)
                .toList();
        if (combinableCards.isEmpty()) {
            event.reply("Você não possui cartas combináveis.").setEphemeral(true).queue();
        } else if (combinableCards.size() == 1) {
            event.reply("Você possui apenas 1 carta combinável.").setEphemeral(true).queue();
        } else {
            consumeInteraction(event);
            var combineQuery = new SelectManyQuery<>(
                    player,
                    "Selecione as cartas para combinar",
                    combinableCards,
                    true,
                    true,
                    card -> DiscordService.getInstance().getEmbedFactory().createCardEmbed(player, card)
            );
            session.query(combineQuery, (q, cards) -> {
                if (cards.isEmpty()) {
                    return;
                }
                if (cards.size() == 1) {
                    player.sendPrivateMessage("Você deve selecionar 2 ou mais cartas para combinar.");
                    return;
                }
                var combinableList = cards.stream().map(CombinableCard.class::cast).toList();
                var combination = new CardCombination(combinableList);
                combination.trigger();
                session.logf("%s combinou %d cartas!", player.getBoldName(), combinableList.size());
            });
        }
    }

}
