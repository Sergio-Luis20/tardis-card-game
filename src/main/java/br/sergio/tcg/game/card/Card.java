package br.sergio.tcg.game.card;

import br.sergio.tcg.Utils;
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Getter
@ToString
@EqualsAndHashCode
@JsonSerialize(using = CardSerializer.class)
@JsonDeserialize(using = CardDeserializer.class)
public abstract sealed class Card permits AttackCard, DefenseCard, EffectCard {

    private String name;
    private Rarity rarity;
    private String description;
    private String imageUrl;

    public Card(String name, Rarity rarity, String description, String imageUrl) {
        Utils.nonNull(name, rarity, description, imageUrl);
        this.name = name;
        this.rarity = rarity;
        this.description = description;
        this.imageUrl = imageUrl;

        try {
            URI.create(imageUrl).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + imageUrl, e);
        }
    }

    public abstract String getType();

    public abstract CompletableFuture<Void> action(TurnDetails turn);

    public static CompletableFuture<Void> completedAction() {
        return CompletableFuture.completedFuture(null);
    }

    public static class CardSerializer extends JsonSerializer<Card> {

        @Override
        public void serialize(Card value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();

            gen.writeStringField("name", value.name);
            gen.writeStringField("rarity", value.rarity.name());
            gen.writeStringField("description", value.description);
            gen.writeStringField("imageUrl", value.imageUrl);
            gen.writeStringField("class", value.getClass().getName());

            gen.writeEndObject();
        }

    }

    public static class CardDeserializer extends JsonDeserializer<Card> {

        @Override
        public Card deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            var node = (JsonNode) p.getCodec().readTree(p);

            var name = node.get("name").asText();
            var rarity = node.get("rarity").asText();
            var description = node.get("description").asText();
            var imageUrl = node.get("image").asText();

            try {
                var cardClass = Class.forName(node.get("class").asText());
                var constructor = cardClass.getConstructor(String.class, Rarity.class, String.class, String.class);
                return (Card) constructor.newInstance(name, Rarity.valueOf(rarity), description, imageUrl);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException | ClassCastException e) {
                throw new IOException("Failed to create CardActionHandler", e);
            }
        }

    }

}
