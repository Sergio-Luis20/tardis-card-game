package br.sergio.tcg.discord.slash;

import br.sergio.tcg.discord.DiscordService;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import static java.util.Objects.requireNonNull;

public record Option(OptionType type, String name, String description, boolean required, Choice[] choices) {

    public Option(OptionType type, String name, String description) {
        this(type, name, description, true, null);
    }

    public Option(OptionType type, String name, String description, boolean required) {
        this(type, name, description, required, null);
    }

    public Option {
        requireNonNull(type, "type");
        requireNonNull(name, "name");
        requireNonNull(description, "description");

        if (!DiscordService.NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid name. Must be between 1 and 32 alphanumeric " +
                    "latin lower case characters plus dash. Provided: " + name + " (" + name.length() + " characters)");
        }

        if (description.isEmpty()) {
            throw new IllegalArgumentException("Empty description. Must be between 1 and 100 characters.");
        }
        if (description.length() > 100) {
            throw new IllegalArgumentException("Too long description. Must be between 1 and 100 characters. " +
                    "Length: " + description.length());
        }

        if (choices != null) {
            if (choices.length < 2) {
                throw new IllegalArgumentException("If you want to use choices with this option, " +
                        "there must exist at least 2. Length: " + choices.length + " array elements.");
            }
            for (Choice choice : choices) {
                requireNonNull(choice, "choice element in array");
            }
        }
    }

    public OptionData optionData() {
        return new OptionData(type, name, description, required, choices != null);
    }

}
