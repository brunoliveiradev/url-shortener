package com.github.vivyteam.service.utils;

import java.math.BigInteger;

public class Base62Encoder {

    public static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final BigInteger BASE = BigInteger.valueOf(ALPHABET.length());

    /**
     * Encodes a long value into a base62 string.
     *
     * @param input the BigInteger value to encode
     * @return the base62 encoded string
     */
    public String encode(BigInteger input) {
        StringBuilder encoded = new StringBuilder();
        BigInteger bigInput = input;

        while (bigInput.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divideAndRemainder = bigInput.divideAndRemainder(BASE);
            bigInput = divideAndRemainder[0];
            int remainder = divideAndRemainder[1].intValue();
            encoded.insert(0, ALPHABET.charAt(remainder));
        }

        return encoded.toString();
    }

    /**
     * Decodes a base62 string into a long value.
     *
     * @param input the base62 encoded string
     * @return the decoded long value
     */
    public long decode(String input) {
        BigInteger decoded = BigInteger.ZERO;

        for (char ch : input.toCharArray()) {
            int index = ALPHABET.indexOf(ch);
            decoded = decoded.multiply(BASE).add(BigInteger.valueOf(index));
        }

        return decoded.longValue();
    }
}
