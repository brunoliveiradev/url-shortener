package com.github.vivyteam.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShorternerApiConfiguration {

    @Value("${app.shortener.url-length}")
    private int urlLength;

    @Value("${app.shortener.domain-url}")
    private String domainUrl;

    public int getUrlLength() {
        return urlLength;
    }

    public String getDomainUrl() {
        return domainUrl;
    }
}
