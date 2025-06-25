package br.sergio.tcg.discord;

import br.sergio.tcg.Utils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageRequest;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public record ImageEmbed(MessageEmbed embed, byte[] imageData, String filename) {

    public ImageEmbed {
        Utils.nonNull(embed);
    }

    public ImageEmbed(MessageEmbed embed) {
        this(embed, null, null);
    }

    public <T extends MessageRequest<T>> T fill(T request) {
        request = request.setEmbeds(embed);
        var fileUpload = fileUpload();
        return fileUpload == null ? request : request.setFiles(fileUpload);
    }

    public MessageCreateAction sendEmbed(MessageChannel channel) {
        var action = channel.sendMessageEmbeds(embed);
        var fileUpload = fileUpload();
        return fileUpload != null ? action.setFiles(fileUpload) : action;
    }

    public FileUpload fileUpload() {
        if (imageData == null) {
            return null;
        } else if (filename == null) {
            var newFileName = UUID.randomUUID().toString();
            return FileUpload.fromData(imageData, newFileName);
        } else {
            return FileUpload.fromData(imageData, filename);
        }
    }

    public static <T extends MessageRequest<T>> T fill(T request, Collection<ImageEmbed> imageEmbeds) {
        var embeds = imageEmbeds.stream()
                .map(ImageEmbed::embed)
                .toList();
        request = request.setEmbeds(embeds);
        var fileUploads = imageEmbeds.stream()
                .map(ImageEmbed::fileUpload)
                .filter(Objects::nonNull)
                .toList();
        return fileUploads.isEmpty() ? request : request.setFiles(fileUploads);
    }

    public static MessageCreateAction sendEmbeds(MessageChannel channel, Collection<ImageEmbed> imageEmbeds) {
        var embeds = imageEmbeds.stream()
                .map(ImageEmbed::embed)
                .toList();
        var request = channel.sendMessageEmbeds(embeds);
        var fileUploads = imageEmbeds.stream()
                .map(ImageEmbed::fileUpload)
                .filter(Objects::nonNull)
                .toList();
        return fileUploads.isEmpty() ? request : request.setFiles(fileUploads);
    }

}
