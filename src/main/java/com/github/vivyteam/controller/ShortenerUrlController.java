package com.github.vivyteam.controller;

import com.github.vivyteam.configuration.ShorternerApiConfiguration;
import com.github.vivyteam.exception.InvalidShortenedUrlException;
import com.github.vivyteam.model.RequestUrlEntity;
import com.github.vivyteam.model.UrlModel;
import com.github.vivyteam.service.UrlShorteningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@RestController
@RequestMapping("")
@Tag(name = "URL Shortening Service", description = "API for shortening and retrieving URLs")
public class ShortenerUrlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShortenerUrlController.class);

    private final UrlShorteningService urlShorteningService;

    private final ShorternerApiConfiguration apiConfiguration;


    public ShortenerUrlController(UrlShorteningService urlShorteningService, ShorternerApiConfiguration apiConfiguration) {
        this.urlShorteningService = urlShorteningService;
        this.apiConfiguration = apiConfiguration;
    }

    @Operation(summary = "Shorten a URL")
    @ApiResponse(responseCode = "201", description = "URL shortened successfully",
            content = @Content(schema = @Schema(implementation = UrlModel.class)))
    @PostMapping(value = "/shorten-url", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UrlModel> shortenUrl(@RequestBody RequestUrlEntity originalUrl) {
        validateLongUrl(originalUrl.originalUrl());

        return urlShorteningService.createShortUrl(originalUrl.originalUrl());
    }

    @Operation(summary = "Get the original URL by its short version")
    @ApiResponse(responseCode = "200", description = "Original URL found",
            content = @Content(schema = @Schema(implementation = UrlModel.class)))
    @GetMapping(value = "/original/{shortUrlId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UrlModel> getOriginalUrl(@PathVariable("shortUrlId") String shortUrlId) {
        validateShortUrlId(shortUrlId);

        return urlShorteningService.findOriginalUrlByShortUrl(apiConfiguration.getDomainUrl() + shortUrlId);
    }

    @Operation(summary = "Redirect to the original URL by its short version")
    @ApiResponse(responseCode = "302", description = "Redirect to the original URL")
    @GetMapping(value = "/{shortUrlId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.FOUND)
    public Mono<Void> redirectToOriginalUrl(@Valid @PathVariable("shortUrlId") String shortUrlId, ServerWebExchange exchange) {
        validateShortUrlId(shortUrlId);

        return (urlShorteningService.findShortUrlAndRedirect(apiConfiguration.getDomainUrl() + shortUrlId))
                .flatMap(urlModel -> {
                    exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                    exchange.getResponse().getHeaders().setLocation(URI.create(urlModel.originalUrl()));
                    return exchange.getResponse().setComplete();
                });
    }

    private void validateShortUrlId(String shortUrlId) {
        // Check if the short URL ID is valid by checking if it has the minimum length of 7
        if (shortUrlId.length() < apiConfiguration.getUrlLength()) {
            LOGGER.info("Invalid shortened URL argument received: {}", shortUrlId);
            throw new InvalidShortenedUrlException("Invalid shortened URL argument: " + shortUrlId);
        }
    }

    private void validateLongUrl(String longUrl) {
        try {
            new URL(longUrl).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.debug("Invalid URL argument received at request: {}", longUrl);
            throw new IllegalArgumentException("Invalid URL argument: " + longUrl);
        }
    }
}
