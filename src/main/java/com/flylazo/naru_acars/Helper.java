package com.flylazo.naru_acars;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Helper {

    public static String generateRandomHash(int length) {
        Random rand = new Random();
        return rand.ints('0', 'z')
                .filter(Character::isLetterOrDigit)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * @param instant1 {@link DateTimeFormatter#ISO_INSTANT}
     * @param instant2 {@link DateTimeFormatter#ISO_INSTANT}
     * @return absolute duration between two instants
     */
    public static Duration getDuration(String instant1, String instant2) {
        Instant p1 = Instant.parse(instant1);
        Instant p2 = Instant.parse(instant2);
        return Duration.between(p1, p2).abs();
    }

}
