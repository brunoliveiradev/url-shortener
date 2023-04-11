package com.github.vivyteam.controller;

import com.github.vivyteam.configuration.ShorternerApiConfiguration;
import com.github.vivyteam.exception.InvalidShortenedUrlException;
import com.github.vivyteam.exception.UrlNotFoundException;
import com.github.vivyteam.model.UrlModel;
import com.github.vivyteam.service.UrlShorteningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@WebFluxTest(ShortenerUrlController.class)
class ShortenerUrlControllerTest {


    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UrlShorteningService urlShorteningService;

    @MockBean
    private ShorternerApiConfiguration apiConfiguration;

    private static final String SHORTEN_URL = "/shorten-url";
    private final String longUrl = "https://goo.gl/maps/pRUToXUPmTvYwyAb9";
    private final String shortenedUrl = "https://myservicedomain.de/5g2IXsE3vG";
    private final String shortenedUrlId = "5g2IXsE3vG";
    private final String nonExistentShortUrlId = "1234AbCd";
    private final String invalidShortUrlId = "123";


    @BeforeEach
    void setUp() {
        when(apiConfiguration.getDomainUrl()).thenReturn("https://myservicedomain.de/");
        when(urlShorteningService.createShortUrl(longUrl)).thenReturn(Mono.just(new UrlModel(longUrl, shortenedUrl)));
    }

    @Test
    @DisplayName("shortenUrl - Should return 201 when the url is valid")
    void shortenValidUrl() {
        webTestClient.post()
                .uri(SHORTEN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue("{\"originalUrl\": \"" + longUrl + "\"}"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.shortenedUrl").isEqualTo(shortenedUrl);
    }

    @Test
    @DisplayName("shortenUrl - Should return 400 when the url is invalid")
    void shortenInvalidUrl() {
        String invalidUrl = "invalid-url";

        webTestClient.post()
                .uri(SHORTEN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue("{\"originalUrl\": \"" + invalidUrl + "\"}"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("shortenUrl - Should return the same shortened url when the url is already shortened")
    void shortenSameUrlTwice() {
        webTestClient.post()
                .uri(SHORTEN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue("{\"originalUrl\": \"" + longUrl + "\"}"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.shortenedUrl").isEqualTo(shortenedUrl);

        webTestClient.post()
                .uri("/shorten-url")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue("{\"originalUrl\": \"" + longUrl + "\"}"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.shortenedUrl").isEqualTo(shortenedUrl);

        Mockito.verify(urlShorteningService, times(2)).createShortUrl(longUrl);
    }


    @Test
    @DisplayName("getOriginalUrl - Should return 200 when the shortened url is valid")
    void getOriginalUrlValidShortenedUrl() {
        when(urlShorteningService.findOriginalUrlByShortUrl(anyString()))
                .thenReturn(Mono.just(new UrlModel(longUrl, shortenedUrl)));

        webTestClient.get()
                .uri("/original/" + shortenedUrlId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.originalUrl").isEqualTo(longUrl)
                .jsonPath("$.shortenedUrl").isEqualTo(shortenedUrl);
    }

    @Test
    @DisplayName("getOriginalUrl - Should return 400 when the shortened url is invalid")
    void getOriginalUrlInvalidShortenedUrl() {
        when(urlShorteningService.findOriginalUrlByShortUrl(anyString()))
                .thenThrow(new InvalidShortenedUrlException("Invalid shortened URL argument: " + invalidShortUrlId));

        webTestClient.get()
                .uri("/original/" + invalidShortUrlId)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(response -> assertEquals("Invalid shortened URL argument: " + invalidShortUrlId, response));
    }

    @Test
    @DisplayName("getOriginalUrl - Should return 404 when the shortened url is not found")
    void getOriginalUrlNonExistentShortenedUrl() {
        String nonExistentShortUrl = "https://myservicedomain.de/1234AbCd";

        when(urlShorteningService.findOriginalUrlByShortUrl(anyString()))
                .thenReturn(Mono.error(new UrlNotFoundException("Unique Identifier of URL not found: " + nonExistentShortUrl)));

        webTestClient.get()
                .uri("/original/" + nonExistentShortUrlId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class).isEqualTo("Unique Identifier of URL not found: " + nonExistentShortUrl);
    }

    @Test
    @DisplayName("redirectToOriginalUrl - Should redirect to the original URL when the shortened URL is valid")
    void redirectToOriginalUrlValidShortenedUrl() {
        when(urlShorteningService.findShortUrlAndRedirect(apiConfiguration.getDomainUrl() + shortenedUrlId))
                .thenReturn(Mono.just(new UrlModel(longUrl, shortenedUrl)));

        webTestClient.get()
                .uri("/" + shortenedUrlId)
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location", longUrl);
    }

    @Test
    @DisplayName("redirectToOriginalUrl - Should return 400 when the shortened URL is invalid")
    void redirectToOriginalUrlInvalidShortenedUrl() {
        when(urlShorteningService.findShortUrlAndRedirect(apiConfiguration.getDomainUrl() + invalidShortUrlId))
                .thenReturn(Mono.error(new InvalidShortenedUrlException("Invalid shortened URL argument: " + invalidShortUrlId)));

        webTestClient.get()
                .uri("/" + invalidShortUrlId)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Invalid shortened URL argument: " + invalidShortUrlId);
    }

    @Test
    @DisplayName("redirectToOriginalUrl - Should return 404 when the shortened URL does not exist")
    void redirectToOriginalUrlNonExistentShortenedUrl() {
        when(urlShorteningService.findShortUrlAndRedirect(apiConfiguration.getDomainUrl() + nonExistentShortUrlId))
                .thenReturn(Mono.error(new UrlNotFoundException("Unique Identifier of URL not found: " + nonExistentShortUrlId)));

        webTestClient.get()
                .uri("/" + nonExistentShortUrlId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class).isEqualTo("Unique Identifier of URL not found: " + nonExistentShortUrlId);
    }

}