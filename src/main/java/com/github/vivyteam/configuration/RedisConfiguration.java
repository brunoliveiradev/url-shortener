package com.github.vivyteam.configuration;

import com.github.vivyteam.model.UrlEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        redisConfig.setPassword(redisPassword);
        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public ReactiveRedisTemplate<String, UrlEntity> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        RedisSerializationContext.RedisSerializationContextBuilder<String, UrlEntity> builder = RedisSerializationContext.newSerializationContext();

        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        RedisSerializer<UrlEntity> urlEntitySerializer = new Jackson2JsonRedisSerializer<>(UrlEntity.class);

        builder.key(stringSerializer)
                .value(urlEntitySerializer)
                .hashKey(stringSerializer)
                .hashValue(urlEntitySerializer);

        RedisSerializationContext<String, UrlEntity> serializationContext = builder.build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

}
