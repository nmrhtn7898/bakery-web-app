package com.bread.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.embedded.RedisServer;

import javax.annotation.PreDestroy;

import java.util.HashMap;
import java.util.Map;

import static java.time.Duration.ofMinutes;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;
import static org.springframework.util.SocketUtils.findAvailableTcpPort;

@EnableCaching
@Profile(value = {"default", "test"})
@Configuration
public class RedisConfig {

    @Value("${spring.redis.port}")
    private final int port;

    private final RedisServer redisServer;

    public RedisConfig(@Value("${spring.profiles.active:default}") String profiles,
                       @Value("${spring.redis.port}") int port) {
        this.port = profiles.equals("test") ? findAvailableTcpPort() : port;
        redisServer = new RedisServer(this.port);
        redisServer.start();
    }

    @PreDestroy
    public void preDestroy() {
        if (redisServer.isActive()) {
            redisServer.stop();
        }
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<byte[], byte[]> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> {
            Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>();
            configurationMap.put("client", defaultCacheConfig().entryTtl(ofMinutes(10)));
            builder.withInitialCacheConfigurations(configurationMap);
        };
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(@Value("${spring.redis.host}") String host) {
        return new LettuceConnectionFactory(host, port);
    }

}
