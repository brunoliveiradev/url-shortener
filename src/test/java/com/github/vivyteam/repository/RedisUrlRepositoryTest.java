package com.github.vivyteam.repository;

import com.github.vivyteam.configuration.RedisConfiguration;
import com.github.vivyteam.model.UrlModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.UUID;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@Testcontainers
class RedisUrlRepositoryTest {

    @Autowired
    private RedisUrlRepository redisUrlRepository;

    @Container
    public static GenericContainer<?> redisContainer = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379);

    @TestConfiguration
    static class TestRedisConfiguration extends RedisConfiguration {

        @Value("${spring.redis.password}")
        private String redisPassword;

        @Bean
        @Primary
        public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
            String redisContainerHost = redisContainer.getHost();
            int redisContainerPort = redisContainer.getFirstMappedPort();

            RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisContainerHost, redisContainerPort);
            redisConfig.setPassword(redisPassword);
            return new LettuceConnectionFactory(redisConfig);
        }

    }
    private UrlModel urlModel;

    @BeforeEach
    void setUp() {
        urlModel = new UrlModel("https://goo.gl/maps/pRUToXUPmTvYwyAb9", "https://myservicedomain.de/" + UUID.randomUUID());
    }

    @Test
    @DisplayName("save - Should save a new URL mapping")
    void testSave() {
        StepVerifier.create(redisUrlRepository.save(urlModel))
                .expectNext(urlModel)
                .verifyComplete();
    }

    @Test
    @DisplayName("findByShortenedUrl - Should find the URL mapping by its shortened URL")
    void testFindByShortenedUrl() {
        redisUrlRepository.save(urlModel).block();

        StepVerifier.create(redisUrlRepository.findByShortenedUrl(urlModel.shortenedUrl()))
                .expectNext(urlModel)
                .verifyComplete();
    }

    @Test
    @DisplayName("findByOriginalUrl - Should find the URL mapping by its original URL")
    void testFindByOriginalUrl() {
        redisUrlRepository.save(urlModel).block();

        StepVerifier.create(redisUrlRepository.findByOriginalUrl(urlModel.originalUrl()))
                .expectNext(urlModel)
                .verifyComplete();
    }


    @Test
    @DisplayName("findByOriginalUrl - Should find the URL mapping by its original URL and cache the result")
    void findByOriginalUrlOnCache() {
        UrlModel urlModel = new UrlModel("https://example.com/long-url", "https://short.url/short");
        redisUrlRepository.save(urlModel).block();

        StepVerifier.create(redisUrlRepository.findByOriginalUrl(urlModel.originalUrl()))
                .expectNextMatches(foundUrl -> foundUrl.originalUrl().equals(urlModel.originalUrl()) &&
                        foundUrl.shortenedUrl().equals(urlModel.shortenedUrl()))
                .verifyComplete();

        // The cache should have the result after the first call
        StepVerifier.create(redisUrlRepository.findByOriginalUrl(urlModel.originalUrl()))
                .expectNextMatches(foundUrl -> foundUrl.originalUrl().equals(urlModel.originalUrl()) &&
                        foundUrl.shortenedUrl().equals(urlModel.shortenedUrl()))
                .verifyComplete();
    }


}