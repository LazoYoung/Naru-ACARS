package com.flylazo.naru_acars;

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

}
