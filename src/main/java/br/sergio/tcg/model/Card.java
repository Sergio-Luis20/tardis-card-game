package br.sergio.tcg.model;

import br.sergio.tcg.model.Card.CardDeserializer;
import br.sergio.tcg.model.Card.CardSerializer;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import lombok.ToString;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Objects.requireNonNull;

@Getter
@ToString
@EqualsAndHashCode()
@JsonSerialize(using = CardSerializer.class)
@JsonDeserialize(using = CardDeserializer.class)
public abstract sealed class Card permits AttackCard, DefenseCard, EffectCard {

    private static final Map<Long, Card> byId = new ConcurrentHashMap<>();
    private static final Map<String, Card> byName = new ConcurrentHashMap<>();

    private String name;
    private Rarity rarity;
    private String description;
    private BufferedImage image;

    @Exclude
    private long id;

    public Card(String name, Rarity rarity, String description, BufferedImage image) {
        checkName(name);

        this.name = requireNonNull(name, "name");
        this.rarity = requireNonNull(rarity, "rarity");
        this.description = requireNonNull(description, "description");
        this.image = requireNonNull(image, "image");
        this.id = generateUniqueId();

        byId.put(id, this);
        byName.put(name, this);
    }

    public abstract String getType();

    private void checkName(String name) {
        if (byName.containsKey(name)) {
            throw new IllegalArgumentException("Card with name \"" + name + "\" already exists");
        }
    }

    public static Optional<Card> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    public static Optional<Card> findByName(String name) {
        return Optional.ofNullable(byName.get(name));
    }

    public static boolean existsById(long id) {
        return byId.containsKey(id);
    }

    public static boolean existsByName(String name) {
        return byName.containsKey(name);
    }

    public static Set<Card> cards() {
        return new HashSet<>(byId.values());
    }

    public static long generateUniqueId() {
        var random = ThreadLocalRandom.current();
        long id;
        do {
            id = random.nextLong();
        } while (byId.containsKey(id));
        return id;
    }

    public static class CardSerializer extends JsonSerializer<Card> {

        @Override
        public void serialize(Card value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();

            gen.writeStringField("name", value.name);
            gen.writeStringField("rarity", value.rarity.name());
            gen.writeStringField("description", value.description);
            gen.writeStringField("image", convertImageToBase64(value.image));
            gen.writeStringField("class", value.getClass().getName());

            gen.writeEndObject();
        }

        private String convertImageToBase64(BufferedImage image) throws IOException {
            try (var stream = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", stream);
                byte[] buffer = stream.toByteArray();
                return Base64.getEncoder().encodeToString(buffer);
            }
        }

    }

    public static class CardDeserializer extends JsonDeserializer<Card> {

        @Override
        public Card deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            var node = (JsonNode) p.getCodec().readTree(p);

            var name = node.get("name").asText();
            var rarity = node.get("rarity").asText();
            var description = node.get("description").asText();
            var imageData = node.get("image").asText();

            try {
                var cardClass = Class.forName(node.get("class").asText());
                var constructor = cardClass.getConstructor(String.class, Rarity.class, String.class, BufferedImage.class);
                return (Card) constructor.newInstance(name, Rarity.valueOf(rarity), description, convertBase64ToImage(imageData));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException | ClassCastException e) {
                throw new IOException("Failed to create CardActionHandler", e);
            }
        }

        private BufferedImage convertBase64ToImage(String base64) throws IOException {
            byte[] buffer = Base64.getDecoder().decode(base64);
            try (var in = new ByteArrayInputStream(buffer)) {
                var image = ImageIO.read(in);
                if (image == null) {
                    throw new IOException("Could not decode image from base64 data");
                }
                return image;
            }
        }

    }

}
