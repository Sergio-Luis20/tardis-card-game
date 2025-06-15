package br.sergio.tcg.discord;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public record CardEmbed(MessageEmbed embed, byte[] imageData, String filename) {

    public void send() {
        DiscordService.getInstance()
                .getGameChannel()
                .sendFiles(FileUpload.fromData(imageData, filename))
                .setEmbeds(embed)
                .queue(null, t -> log.error("Failed to send card embed on game channel", t));
    }

    public static void sendAll(MessageChannel channel, List<CardEmbed> cardEmbeds) {
        var embeds = new ArrayList<MessageEmbed>(cardEmbeds.size());
        var uploads = new ArrayList<FileUpload>(cardEmbeds.size());
        for (var embed : cardEmbeds) {
            embeds.add(embed.embed);
            uploads.add(FileUpload.fromData(embed.imageData, embed.filename));
        }
        channel.sendFiles(uploads).setEmbeds(embeds).queue(null, t -> {
            log.error("Failed to set card embeds on channel {} ({})", channel.getName(), channel.getId(), t);
        });
    }

}
