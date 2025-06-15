package br.sergio.tcg;

import java.awt.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

}
