package com.github.vivyteam.service.utils;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class Base62EncoderTest {

    private final Base62Encoder base62Encoder = new Base62Encoder();

    @Test
    public void testEncode() {
        BigInteger input = BigInteger.valueOf(123456789L);
        String expectedEncodedValue = "8m0Kx";
        String encodedValue = base62Encoder.encode(input);
        assertEquals(expectedEncodedValue, encodedValue, "Encoded value should match the expected value");
    }

    @Test
    public void testDecode() {
        String encodedValue = "8m0Kx";
        long expectedDecodedValue = 123456789L;
        long decodedValue = base62Encoder.decode(encodedValue);
        assertEquals(expectedDecodedValue, decodedValue, "Decoded value should match the expected value");
    }

    @Test
    public void testEncodeDecode() {
        BigInteger input = BigInteger.valueOf(123456789L);
        String encodedValue = base62Encoder.encode(input);
        long decodedValue = base62Encoder.decode(encodedValue);
        assertEquals(input.longValue(), decodedValue, "Decoded value should match the original input value");
    }
}