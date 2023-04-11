package com.github.vivyteam.exception;

public class InvalidShortenedUrlException extends RuntimeException {
    public InvalidShortenedUrlException(String message) {
        super(message);
    }
}