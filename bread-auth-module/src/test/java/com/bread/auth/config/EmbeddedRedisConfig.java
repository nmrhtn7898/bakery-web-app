package com.bread.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import redis.embedded.RedisServer;

import javax.annotation.PreDestroy;

import static java.util.Arrays.asList;
import static org.springframework.util.SocketUtils.findAvailableTcpPort;

@EnableRedisRepositories
@TestConfiguration
public class EmbeddedRedisConfig {

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
