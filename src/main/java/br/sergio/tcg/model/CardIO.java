package br.sergio.tcg.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CardIO {

    private static final ObjectMapper mapper = new ObjectMapper();

    private CardIO() {
        throw new UnsupportedOperationException();
    }

    public static void saveAll(List<Card> cards, Path file) throws IOException {
        try (var out = new BufferedOutputStream(Files.newOutputStream(file))) {
            mapper.writeValue(out, cards);
        }
    }

    public static List<Card> loadAll(Path file) throws IOException {
        try (var in = new BufferedInputStream(Files.newInputStream(file))) {
            return mapper.readValue(in, new TypeReference<>() {});
        }
    }

}
