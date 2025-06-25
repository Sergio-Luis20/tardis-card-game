package br.sergio.tcg.game.card;

import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.Card.CardDeserializer;
import br.sergio.tcg.game.card.Card.CardSerializer;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@JsonSerialize(using = CardSerializer.class)
@JsonDeserialize(using = CardDeserializer.class)
public abstract sealed class Card permits AttackCard, DefenseCard, EffectCard {

    @NonNull
    private String name;

    @NonNull
    private Rarity rarity;

    @NonNull
    private String description;

    @NonNull
    private BufferedImage image;

    public abstract String getType();

    public abstract CompletableFuture<Void> action(TurnDetails turn);

    public static CompletableFuture<Void> completedAction() {
        return CompletableFuture.completedFuture(null);
    }

    public static BufferedImage getImage(String filename) {
        var resourceName = "/card-images/" + filename;
        var stream = Card.class.getResourceAsStream(resourceName);
        Objects.requireNonNull(stream, "Resource not found: " + resourceName);
        try (var buff = new BufferedInputStream(stream)) {
            return ImageIO.read(buff);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read resource image: " + resourceName, e);
        }
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
