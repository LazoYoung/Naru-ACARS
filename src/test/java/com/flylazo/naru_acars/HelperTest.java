package com.flylazo.naru_acars;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

import java.time.Duration;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

class HelperTest {

    @RepeatedTest(100)
    @Execution(SAME_THREAD)
    void generateRandomHash() {
        int length = new Random().nextInt(10, 20);
        String value = Helper.generateRandomHash(length);

        for (int c : value.toCharArray()) {
            boolean isAlphabet = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
            boolean isNumeric = (c >= '0' && c <= '9');
            boolean isAlphaNumeric = isAlphabet || isNumeric;
            assertTrue(isAlphaNumeric);
        }
    }
}