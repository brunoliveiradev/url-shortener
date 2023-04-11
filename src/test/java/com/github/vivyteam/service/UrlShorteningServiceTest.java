package com.github.vivyteam.service;

import com.github.vivyteam.configuration.ShorternerApiConfiguration;
import com.github.vivyteam.exception.UrlNotFoundException;
import com.github.vivyteam.model.UrlModel;
import com.github.vivyteam.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlShorteningServiceTest {

    private UrlRepository urlRepository;
    private ShorternerApiConfiguration apiConfiguration;
    private UrlShorteningService urlShorteningService;

    private final String originalUrl = "https://goo.gl/maps/pRUToXUPmTvYwyAb9";
    private final String shortUrl = "https://myservicedomain.de/5g2IXsE3vG";

    @BeforeEach
    public void setUp() {
        urlRepository = Mockito.mock(UrlRepository.class);
        apiConfiguration = Mockito.mock(ShorternerApiConfiguration.class);
        urlShorteningService = new UrlShorteningService(urlRepository, apiConfiguration);
    }

    @Test
    @DisplayName("createShortUrl should return a UrlModel with the original and shortened url")
    public void testCreateShortUrl() {
        UrlModel urlModel = new UrlModel(originalUrl, shortUrl);

        // Mock repository and configuration behavior
        when(urlRepository.findByOriginalUrl(originalUrl)).thenReturn(Mono.empty());
        when(urlRepository.save(any(UrlModel.class))).thenReturn(Mono.just(urlModel));
        when(apiConfiguration.getUrlLength()).thenReturn(7);
        when(apiConfiguration.getDomainUrl()).thenReturn("https://short.ly/");

        // Test createShortUrl method
        StepVerifier.create(urlShorteningService.createShortUrl(originalUrl))
                .expectNextMatches(result -> result.originalUrl().equals(originalUrl)
                        && result.shortenedUrl().equals(shortUrl))
                .verifyComplete();
    }

    @Test
    @DisplayName("findOriginalUrlByShortUrl should return a UrlModel with the original and shortened url")
    public void testFindOriginalUrlByShortUrl() {
        UrlModel urlModel = new UrlModel(originalUrl, shortUrl);

        when(urlRepository.findByShortenedUrl(shortUrl)).thenReturn(Mono.just(urlModel));

        StepVerifier.create(urlShorteningService.findOriginalUrlByShortUrl(shortUrl))
                .expectNextMatches(result -> result.originalUrl().equals(originalUrl)
                        && result.shortenedUrl().equals(shortUrl))
                .verifyComplete();
    }

    @Test
    @DisplayName("findOriginalUrlByShortUrl should throw an UrlNotFoundException")
    public void testFindOriginalUrlByShortUrlNotFound() {
        when(urlRepository.findByShortenedUrl(shortUrl)).thenReturn(Mono.empty());

        StepVerifier.create(urlShorteningService.findOriginalUrlByShortUrl(shortUrl))
                .expectError(UrlNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("findShortUrlAndRedirect should return a UrlModel with the original and shortened url")
    public void testFindShortUrlAndRedirect() {
        UrlModel urlModel = new UrlModel(originalUrl, shortUrl);

        when(urlRepository.findByShortenedUrl(shortUrl)).thenReturn(Mono.just(urlModel));

        StepVerifier.create(urlShorteningService.findShortUrlAndRedirect(shortUrl))
                .expectNextMatches(result -> result.originalUrl().equals(originalUrl)
                        && result.shortenedUrl().equals(shortUrl))
                .verifyComplete();
    }

    @Test
    @DisplayName("findShortUrlAndRedirect should throw an UrlNotFoundException")
    public void testFindShortUrlAndRedirectNotFound() {
        when(urlRepository.findByShortenedUrl(shortUrl)).thenReturn(Mono.empty());

        StepVerifier.create(urlShorteningService.findShortUrlAndRedirect(shortUrl))
                .expectError(UrlNotFoundException.class)
                .verify();
    }

}