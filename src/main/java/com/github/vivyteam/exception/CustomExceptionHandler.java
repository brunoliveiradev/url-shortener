package com.github.vivyteam.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Component
@ControllerAdvice
public class CustomExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @ExceptionHandler(InvalidShortenedUrlException.class)
    public Mono<ResponseEntity<String>> handleInvalidShortUrlException(InvalidShortenedUrlException e) {
        LOGGER.info(e.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()));
    }

    @ExceptionHandler(UrlNotFoundException.class)
    public Mono<ResponseEntity<String>> handleUrlNotFoundException(UrlNotFoundException e) {
        LOGGER.info(e.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, NullPointerException.class})
    public Mono<ResponseEntity<String>> handleSpecificExceptions(RuntimeException e) {
        LOGGER.info(e.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public Mono<ResponseEntity<String>> handleNoSuchElementExceptions(RuntimeException e) {
        LOGGER.info(e.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> handleUnknownExceptions(Exception e) {
        LOGGER.error(e.getMessage());
        String message = e.getMessage() != null ? e.getMessage() : "Internal server error";
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message));
    }

}
