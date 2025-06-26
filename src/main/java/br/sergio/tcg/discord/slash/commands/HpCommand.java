package br.sergio.tcg.discord.slash.commands;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.slash.Option;
import br.sergio.tcg.discord.slash.SlashCommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;

@Slf4j
public class HpCommand extends SlashCommand {

    public HpCommand() {
        super("hp", "Mostra sua vida atual");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event, Member member) {
        var opt = DiscordService.getInstance().getSessionManager().findSession(member);
        if (opt.isEmpty()) {
            event.reply("Você não está em partida").setEphemeral(true).queue();
            return;
        }
        var session = opt.get();
        var player = session.findByMember(member).orElseThrow();
        var targetMember = event.getOption("player", player.getMember(), OptionMapping::getAsMember);
        session.findByMember(targetMember).ifPresentOrElse(target -> {
            var message = target.equals(player) ? "Sua vida atual é **" + target.getHp() + "**."
                    : "A vida atual de " + target.getBoldName() + " é **" + target.getHp() + "**.";
            event.reply(message).queue(null,
                    t -> log.error("Failed to send message to {}", player.getName(), t));
        }, () -> event.reply("Jogador não encontrado na mesma partida que a sua: "
                + targetMember.getEffectiveName() + ".").queue(null,
                t -> log.error("Failed to send message to {}", player.getName(), t)));
    }

    @Override
    public List<Option> options() {
        var target = new Option(OptionType.USER, "player", "Mostra a vida de um jogador. Se ele não for especificado, mostra sua própria vida.", false);
        return List.of(target);
    }

}
