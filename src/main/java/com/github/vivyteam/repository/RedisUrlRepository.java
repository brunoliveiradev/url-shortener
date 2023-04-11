package com.github.vivyteam.repository;

import com.github.vivyteam.model.UrlEntity;
import com.github.vivyteam.model.UrlModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
public class RedisUrlRepository implements UrlRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUrlRepository.class);
    private static final String REDIS_KEY = "shortener:url";
    private static final String REDIS_KEY_SECONDARY_INDEX = "url:index";
    private static final String REDIS_KEY_FIND_BY_ORIGINAL_URL_CACHE = "shortener:url:originalUrlCache";

    @Value("${app.shortener.cache-expiration-time}")
    private Duration cacheExpirationTime;

    private final ReactiveHashOperations<String, String, UrlEntity> reactiveHashOperations;
    private final ReactiveValueOperations<String, UrlEntity> reactiveValueOperations;

    public RedisUrlRepository(ReactiveRedisTemplate<String, UrlEntity> reactiveRedisTemplate) {
        this.reactiveHashOperations = reactiveRedisTemplate.opsForHash();
        this.reactiveValueOperations = reactiveRedisTemplate.opsForValue();
    }

    @Override
    public Mono<UrlModel> save(UrlModel urlModel) {
        UrlEntity urlEntity = UrlEntity.fromModel(urlModel);
        LOGGER.debug("Saving URL: {}", urlEntity);
        return reactiveHashOperations.put(REDIS_KEY, urlEntity.getShortenedUrl(), urlEntity)
                .then(reactiveHashOperations.put(REDIS_KEY_SECONDARY_INDEX, urlEntity.getOriginalUrl(), urlEntity))
                .doOnSuccess(success -> LOGGER.debug("URL saved successfully: {}", urlEntity))
                .thenReturn(urlEntity.toModel());
    }

    @Override
    public Mono<UrlModel> findByShortenedUrl(String shortenedUrl) {
        LOGGER.debug("Searching for URL with shortened URL: {}", shortenedUrl);
        return reactiveHashOperations.get(REDIS_KEY, shortenedUrl)
                .doOnNext(urlEntity -> LOGGER.debug("Found URL: {} -> {}", urlEntity.getOriginalUrl(), urlEntity.getShortenedUrl()))
                .map(UrlEntity::toModel);
    }

    // first we try to find in cache, if not found, then we try to find in secondary index, and If its in secondary index, save it in cache
    @Override
    public Mono<UrlModel> findByOriginalUrl(String originalUrl) {
        LOGGER.debug("Searching for URL with original URL: {}", originalUrl);

        return reactiveValueOperations.get(REDIS_KEY_FIND_BY_ORIGINAL_URL_CACHE + originalUrl)
                .switchIfEmpty(
                        reactiveHashOperations.get(REDIS_KEY_SECONDARY_INDEX, originalUrl)
                                .doOnNext(urlEntity -> LOGGER.debug("URL found in secondary index: {} -> {}", urlEntity.getOriginalUrl(), urlEntity.getShortenedUrl()))
                                .flatMap(urlEntity -> reactiveValueOperations.set(
                                                REDIS_KEY_FIND_BY_ORIGINAL_URL_CACHE + originalUrl,
                                                urlEntity,
                                                cacheExpirationTime)
                                        .thenReturn(urlEntity)
                                )
                )
                .doOnNext(urlEntity -> LOGGER.info("URL already created found it: {} -> {}", urlEntity.getOriginalUrl(), urlEntity.getShortenedUrl()))
                .map(UrlEntity::toModel);
    }
}
