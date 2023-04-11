package com.github.vivyteam.repository;

import com.github.vivyteam.model.UrlModel;
import reactor.core.publisher.Mono;

public interface UrlRepository {

    Mono<UrlModel> save(UrlModel urlModel);

    Mono<UrlModel> findByShortenedUrl(String shortenedUrl);

    Mono<UrlModel> findByOriginalUrl(String originalUrl);
}
