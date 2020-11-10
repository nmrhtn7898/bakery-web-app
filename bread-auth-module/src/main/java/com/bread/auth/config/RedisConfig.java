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
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;
import static org.springframework.util.SocketUtils.findAvailableTcpPort;

@EnableRedisRepositories
@Configuration
public class RedisConfig {

    @Configuration
    @Profile(value = {"default", "test"})
    public static class EmbeddedRedisConfig {

        private final RedisServer redisServer;

        private final int port;

        public EmbeddedRedisConfig(Environment environment, @Value("${spring.redis.port}") int port) {

            this.port = asList(environment.getActiveProfiles()).contains("test") ? findAvailableTcpPort() : port;
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
        public RedisConnectionFactory redisConnectionFactory(@Value("${spring.redis.host}") String host) {
            return new LettuceConnectionFactory(host, port);
        }

    }

    @Configuration
    @Profile(value = {"dev", "prod"})
    public static class ExternalRedisConfig {

        @Bean
        public RedisConnectionFactory redisConnectionFactory(@Value("${spring.redis.host}") String host,
                                                             @Value("${spring.redis.port}") int port) {
            return new LettuceConnectionFactory(host, port);
        }

        @Bean
        public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
            RedisTemplate<byte[], byte[]> redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(redisConnectionFactory);
            return redisTemplate;
        }

    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> {
            Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>();
            configurationMap.put("client", defaultCacheConfig().entryTtl(ofSeconds(10)));
            builder.withInitialCacheConfigurations(configurationMap);
        };
    }

}
