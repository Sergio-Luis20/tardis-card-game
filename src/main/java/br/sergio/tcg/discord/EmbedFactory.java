package br.sergio.tcg.discord;

import br.sergio.tcg.Utils;
import br.sergio.tcg.model.card.Card;
import br.sergio.tcg.model.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EmbedFactory {

    public MessageEmbed createRollEmbed(Player player, int roll) {
        if (!(player instanceof DiscordPlayer discordPlayer)) {
            throw new IllegalArgumentException("Player must be an instance of DiscordPlayer");
        }

        var embed = new EmbedBuilder();

        var name = player.getName();

        embed.setAuthor(name, null, discordPlayer.getAvatarUrl());
        embed.setTitle("\uD83C\uDFB2 " + name + " rolou o dado!");
        embed.setDescription(name + " tirou **" + roll + "** no dado!");
        embed.setColor(player.getColor());

        return embed.build();
    }

    public CardEmbed createCardEmbed(Card card) {
        return createCardEmbed(null, card);
    }

    public CardEmbed createCardEmbed(Player cardOwner, Card card) {
        var embed = new EmbedBuilder();

        if (cardOwner == null) {
            var bot = DiscordService.getInstance().getJda().getSelfUser();
            embed.setAuthor(bot.getEffectiveName(), null, bot.getEffectiveAvatarUrl());
        } else if (cardOwner instanceof DiscordPlayer discordPlayer) {
            embed.setAuthor(cardOwner.getName(), null, discordPlayer.getAvatarUrl());
        } else {
            throw new IllegalArgumentException("Player must be an instance of DiscordPlayer");
        }

        embed.setTitle(card.getName());

        embed.addField("Tipo", card.getType(), false);
        embed.addField("Raridade", card.getRarity().translated(), false);
        embed.setColor(cardOwner != null ? cardOwner.getColor() : Utils.randomColor());
        embed.setDescription(card.getDescription());

        var id = Base64.getEncoder().encodeToString(card.getClass().getName().getBytes(StandardCharsets.UTF_8));

        embed.setFooter(id);

        var filename = id + ".png";

        embed.setImage("attachment://" + filename);

        var stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(card.getImage(), "png", stream);
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write card image to buffer", e);
        }

        return new CardEmbed(embed.build(), stream.toByteArray(), filename);
    }

}
