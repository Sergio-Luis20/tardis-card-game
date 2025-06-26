package br.sergio.tcg.discord.slash;

import br.sergio.tcg.discord.DiscordService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
@Getter
public abstract class SlashCommand {

    protected final String name, description;

    public SlashCommand(String name, String description) {
        this.name = requireNonNull(name, "name");
        this.description = requireNonNull(description, "description");

        if (!DiscordService.NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid name. Must be between 1 and 32 alphanumeric " +
                    "lower case characters plus dash. Provided: " + name + " (" + name.length() + " characters)");
        }

        if (description.isEmpty()) {
            throw new IllegalArgumentException("Empty description. Must be between 1 and 100 characters.");
        }
        if (description.length() > 100) {
            throw new IllegalArgumentException("Too long description. Must be between 1 and 100 characters. " +
                    "Length: " + description.length() + " characters.");
        }
    }

    public abstract void onCommand(SlashCommandInteractionEvent event, Member member);

    protected void consumeInteraction(SlashCommandInteractionEvent event) {
        DiscordService.getInstance().consumeInteraction(event);
    }

    public SlashCommandData slashData() {
        var data = Commands.slash(name, description);
        var options = options();
        if (!options.isEmpty()) {
            data.addOptions(options.stream().map(Option::optionData).toList());
        }
        return data;
    }

    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        var options = new HashMap<String, Option>();
        for (Option option : options()) {
            options.put(option.name(), option);
        }
        var focusedOption = event.getFocusedOption();
        var optionName = focusedOption.getName();
        var option = options.get(optionName);
        if (option == null) {
            return;
        }
        var choices = option.choices();
        if (choices == null) {
            return;
        }
        var value = focusedOption.getValue();
        var choiceList = Arrays.stream(choices)
                .filter(choice -> choice.getName().startsWith(optionName))
                .toList();
        event.replyChoices(choiceList).queue(null, t -> {
            log.error("Could not send choices of autocomplete for command {} on option {}", name, optionName, t);
        });
    }

    public List<Option> options() {
        return Collections.emptyList();
    }



}
