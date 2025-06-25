package br.sergio.tcg;

import br.sergio.tcg.game.card.Card;
import br.sergio.tcg.game.card.CardRepository;
import br.sergio.tcg.game.card.NotACardClassException;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Utils {

    public static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private Utils() {
        throw new UnsupportedOperationException();
    }

    public static Color randomColor() {
        var random = ThreadLocalRandom.current();

        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        return new Color(red, green, blue);
    }

    public static int rollDice() {
        return ThreadLocalRandom.current().nextInt(6) + 1;
    }

    public static ZonedDateTime dateTime() {
        return ZonedDateTime.now(ZONE_ID);
    }

    public static String formatCollection(Collection<?> col) {
        List<?> list;
        if (col instanceof List<?> l) {
            list = l;
        } else {
            list = new ArrayList<>(col);
        }
        if (list.size() == 1) {
            return list.getFirst().toString();
        }
        var sublist = list.subList(0, list.size() - 1).stream().map(Object::toString).toList();
        var last = list.getLast();
        return String.join(", ", sublist) + " e " + last;
    }

    public static String className(Object obj) {
        return obj == null ? "null" : obj.getClass().getName();
    }

    @SuppressWarnings("unchecked")
    public static Card fromEmbed(MessageEmbed embed) {
        var id = embed.getFooter().getText();
        var decodedBytes = Base64.getDecoder().decode(id);
        var className = new String(decodedBytes, StandardCharsets.UTF_8);
        try {
            var cardClass = Class.forName(className);
            return CardRepository.getInstance().findByClass((Class<Card>) cardClass);
        } catch (ClassNotFoundException e) {
            throw new NotACardClassException("Not a card class: " + className);
        }
    }

    public static void nonNull(Object... objects) {
        for (var obj : objects) {
            Objects.requireNonNull(obj);
        }
    }

    public static FileUpload loadResource(String resourceLocation) {
        var stream = Utils.class.getResourceAsStream(resourceLocation);
        if (stream == null) {
            throw new RuntimeException("Resource not found: " + resourceLocation);
        }
        try (var buf = new BufferedInputStream(stream)) {
            return FileUpload.fromData(buf.readAllBytes(), "coin.gif");
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read resource " + resourceLocation, e);
        }
    }

}
