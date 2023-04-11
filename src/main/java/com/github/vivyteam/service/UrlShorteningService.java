package com.github.vivyteam.service;

import com.github.vivyteam.configuration.ShorternerApiConfiguration;
import com.github.vivyteam.exception.UrlNotFoundException;
import com.github.vivyteam.model.UrlModel;
import com.github.vivyteam.repository.UrlRepository;
import com.github.vivyteam.service.utils.Base62Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Service
public class UrlShorteningService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlShorteningService.class);
    private final Base62Encoder base62Encoder = new Base62Encoder();
    private final UrlRepository urlRepository;
    private final ShorternerApiConfiguration apiConfiguration;


    public UrlShorteningService(UrlRepository urlRepository,
                                ShorternerApiConfiguration apiConfiguration) {
        this.urlRepository = urlRepository;
        this.apiConfiguration = apiConfiguration;
    }

    public Mono<UrlModel> createShortUrl(String originalUrl) {
        LOGGER.info("Creating short URL for: {}", originalUrl);

        return urlRepository.findByOriginalUrl(originalUrl)
                .switchIfEmpty(tryGenerateShortUrl(originalUrl));
    }

    public Mono<UrlModel> findOriginalUrlByShortUrl(String shortUrl) {
        LOGGER.info("Finding original URL by short URL: {}", shortUrl);

        return urlRepository.findByShortenedUrl(shortUrl)
                .doOnNext(urlModel -> LOGGER.info("Original URL found: {} -> {}", shortUrl, urlModel.originalUrl()))
                .switchIfEmpty(Mono.error(new UrlNotFoundException("Unique Identifier of URL not found: " + shortUrl)));

    }

    public Mono<UrlModel> findShortUrlAndRedirect(String shortUrl) {
        LOGGER.info("Finding short URL for redirection: {}", shortUrl);

        return urlRepository.findByShortenedUrl(shortUrl)
                .doOnNext(urlModel -> LOGGER.info("Short URL found for redirection: {} -> {}", shortUrl, urlModel.originalUrl()))
                .switchIfEmpty(Mono.error(new UrlNotFoundException("Unique Identifier of URL not found: " + shortUrl)));
    }

    private Mono<UrlModel> tryGenerateShortUrl(String originalUrl) {
        return Mono.fromCallable(() -> generateShortUrl(originalUrl))
                .flatMap(urlModel -> urlRepository.save(urlModel)
                        .doOnNext(savedUrlModel -> LOGGER.info("Short URL generated and saved: {} -> {}", originalUrl, savedUrlModel.shortenedUrl())))
                .onErrorResume(e -> {
                    LOGGER.error("Unable to generate a short URL: {}", e.getMessage());
                    return Mono.error(new IllegalArgumentException("Unable to generate a short URL: ", e));
                });
    }

    private UrlModel generateShortUrl(String originalUrl) throws NoSuchAlgorithmException {
        LOGGER.debug("Generating short URL for: {}", originalUrl);

        int length = apiConfiguration.getUrlLength();
        String baseUrl = apiConfiguration.getDomainUrl();

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update((originalUrl).getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();

        // Get a positive 7-byte portion of the digest and convert it to a BigInteger
        BigInteger id = new BigInteger(1, Arrays.copyOfRange(digest, 0, length));
        StringBuilder shortUrlPath = new StringBuilder(base62Encoder.encode(id));

        // Pad the encoded string with the first character of the alphabet to ensure it's always 7 characters long
        while (shortUrlPath.length() < length) {
            shortUrlPath.insert(0, Base62Encoder.ALPHABET.charAt(0));
        }

        String shortUrl = baseUrl + shortUrlPath;
        LOGGER.debug("Generated short URL: {}", shortUrl);

        return new UrlModel(originalUrl, shortUrl);
    }
}
