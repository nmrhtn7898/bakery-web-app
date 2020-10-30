package com.bread.auth.base;

import com.bread.auth.config.EmbeddedRedisConfig;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.Inheritance;

import static org.springframework.util.SocketUtils.findAvailableTcpPort;

@Disabled
@Inheritance
@DataJpaTest
@Import(EmbeddedRedisConfig.class)
@ActiveProfiles("test")
public abstract class AbstractDataJpaTest {

    private final int port = findAvailableTcpPort();

    private final RedisServer redisServer = new RedisServer(port);

    @PostConstruct
    public void postConstruct() {
        redisServer.start();
    }

    @PreDestroy
    public void preDestroy() {
        redisServer.stop();
    }

}
