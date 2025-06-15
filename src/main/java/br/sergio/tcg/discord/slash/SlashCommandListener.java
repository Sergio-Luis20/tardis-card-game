package br.sergio.tcg.discord.slash;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;

public class SlashCommandListener extends ListenerAdapter {

    private Map<String, SlashCommand> commands;

    public SlashCommandListener(List<SlashCommand> commands) {
        this.commands = new HashMap<>(commands.size());

        for (var command : commands) {
            this.commands.put(command.getName(), command);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        var member = event.getMember();
        if (member == null) {
            event.reply("Este comando só pode ser executado num servidor.").setEphemeral(true).queue();
            return;
        }
        getCommand(event.getName()).ifPresent(cmd -> cmd.onCommand(event, member));
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        getCommand(event.getName()).ifPresent(cmd -> cmd.onAutoComplete(event));
    }

    private Optional<SlashCommand> getCommand(String name) {
        return Optional.ofNullable(commands.get(name));
    }

    public Map<String, SlashCommand> getCommands() {
        return Collections.unmodifiableMap(commands);
    }

}
