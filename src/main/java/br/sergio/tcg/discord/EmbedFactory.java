package br.sergio.tcg.discord;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.card.Card;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EmbedFactory {

    public ImageEmbed createRollEmbed(Player player, int roll) {
        var embed = new EmbedBuilder();

        var name = player.getName();

        embed.setAuthor(name, null, player.getAvatarUrl());
        embed.setTitle("\uD83C\uDFB2 " + name + " rolou o dado!");
        embed.setDescription(name + " tirou **" + roll + "** no dado!");
        embed.setColor(player.getColor());

        return new ImageEmbed(embed.build());
    }

    public ImageEmbed createCardEmbed(Card card) {
        return createCardEmbed(null, card);
    }

    public ImageEmbed createCardEmbed(Player cardOwner, Card card) {
        var embed = new EmbedBuilder();

        if (cardOwner == null) {
            var bot = DiscordService.getInstance().getJda().getSelfUser();
            embed.setAuthor(bot.getEffectiveName(), null, bot.getEffectiveAvatarUrl());
        } else {
            embed.setAuthor(cardOwner.getName(), null, cardOwner.getAvatarUrl());
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
        try (var buf = new BufferedOutputStream(stream)) {
            ImageIO.write(card.getImage(), "png", buf);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write card image to buffer", e);
        }

        return new ImageEmbed(embed.build(), stream.toByteArray(), filename);
    }

    public ImageEmbed createIntegerEmbed(int n) {
        var embed = new EmbedBuilder();

        embed.setDescription(Integer.toString(n));
        embed.setColor(Color.WHITE);

        return new ImageEmbed(embed.build());
    }

    public ImageEmbed createPlayerEmbed(Player player) {
        var embed = new EmbedBuilder();

        embed.setTitle(player.getName());
        embed.setImage(player.getAvatarUrl());
        embed.setColor(player.getColor());

        return new ImageEmbed(embed.build());
    }

}
