package com.github.vivyteam.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Objects;

@RedisHash
public class UrlEntity {

    @Id
    private String id;
    private String originalUrl;
    private String shortenedUrl;

    public UrlEntity(String originalUrl, String shortenedUrl) {
        this.originalUrl = originalUrl;
        this.shortenedUrl = shortenedUrl;
    }

    public UrlEntity() {
    }

    public UrlModel toModel() {
        return new UrlModel(originalUrl, shortenedUrl);
    }

    public static UrlEntity fromModel(UrlModel urlModel) {
        return new UrlEntity(urlModel.originalUrl(), urlModel.shortenedUrl());
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortenedUrl() {
        return shortenedUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public void setShortenedUrl(String shortenedUrl) {
        this.shortenedUrl = shortenedUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlEntity urlEntity = (UrlEntity) o;
        return Objects.equals(originalUrl, urlEntity.originalUrl) && Objects.equals(shortenedUrl, urlEntity.shortenedUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalUrl, shortenedUrl);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{ originalUrl='").append(originalUrl).append('\'');
        sb.append(", shortenedUrl='").append(shortenedUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
