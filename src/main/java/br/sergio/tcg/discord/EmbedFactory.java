package br.sergio.tcg.discord;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.Player;
import br.sergio.tcg.game.card.Card;
import br.sergio.tcg.game.card.CombinableCard;
import br.sergio.tcg.game.card.family.CardFamily;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EmbedFactory {

    public MessageEmbed createRollEmbed(Player player, int roll) {
        var embed = new EmbedBuilder();

        embed.setAuthor(player.getName(), null, player.getAvatarUrl());
        embed.setTitle("\uD83C\uDFB2 " + player.getBoldName() + " rolou o dado!");
        embed.setDescription(player.getBoldName() + " tirou **" + roll + "** no dado!");
        embed.setColor(player.getColor());

        return embed.build();
    }

    public MessageEmbed createCardEmbed(Card card) {
        return createCardEmbed(null, card);
    }

    public MessageEmbed createCardEmbed(Player cardOwner, Card card) {
        var embed = new EmbedBuilder();

        if (cardOwner == null) {
            var bot = DiscordService.getInstance().getJda().getSelfUser();
            embed.setAuthor(bot.getEffectiveName(), null, bot.getEffectiveAvatarUrl());
        } else {
            embed.setAuthor(cardOwner.getName(), null, cardOwner.getAvatarUrl());
        }

        embed.setTitle(card.getName());

        embed.addField("Tipo", card.getType(), true);
        embed.addField("Raridade", card.getRarity().translated(), true);
        embed.addField("Combinável", card instanceof CombinableCard ? "Sim" : "Não", true);
        embed.addField("Família(s)", card instanceof CardFamily family ? family.getFamilyName() : "-", false);
        embed.setColor(cardOwner != null ? cardOwner.getColor() : Utils.randomColor());
        embed.setDescription(card.getDescription());

        var id = Base64.getEncoder().encodeToString(card.getClass().getName().getBytes(StandardCharsets.UTF_8));

        embed.setFooter(id);
        embed.setImage(card.getImageUrl());

        return embed.build();
    }

    public MessageEmbed createIntegerEmbed(int n) {
        var embed = new EmbedBuilder();

        embed.setDescription(Integer.toString(n));
        embed.setColor(Color.WHITE);

        return embed.build();
    }

    public MessageEmbed createPlayerEmbed(Player player) {
        var embed = new EmbedBuilder();

        embed.setTitle(player.getName());
        embed.setImage(player.getAvatarUrl());
        embed.setColor(player.getColor());

        return embed.build();
    }

}
