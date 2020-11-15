package com.bread.auth;

import com.bread.auth.entity.Account;
import com.bread.auth.entity.AccountAuthority;
import com.bread.auth.entity.Authority;
import com.bread.auth.entity.Oauth2Client;
import com.bread.common.ErrorsSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServer;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.SocketUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;
import static org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder;
import static org.springframework.util.SocketUtils.findAvailableTcpPort;
import static org.springframework.util.StringUtils.hasText;

@EnableCaching
@SpringBootApplication
public class AuthApplication {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return createDelegatingPasswordEncoder();
    }

    @Profile(value = {"default", "dev"})
    @ConditionalOnProperty(name = "spring.jpa.hibernate.ddl-auto", havingValue = "create")
    @Component
    @RequiredArgsConstructor
    public static class DataInitRunner implements ApplicationRunner {

        private final EntityManager entityManager;

        private final PasswordEncoder passwordEncoder;

        @Transactional
        @Override
        public void run(ApplicationArguments args) throws Exception {
            Authority authority = Authority
                    .builder()
                    .name("user")
                    .build();
            Account user = Account
                    .builder()
                    .email("user")
                    .password(passwordEncoder.encode("user"))
                    .build();
            AccountAuthority accountAuthority = AccountAuthority
                    .builder()
                    .account(user)
                    .authority(authority)
                    .build();
            entityManager.persist(authority);
            entityManager.persist(user);
            entityManager.persist(accountAuthority);

            Oauth2Client client = Oauth2Client
                    .builder()
                    .clientId("test")
                    .clientSecret("{noop}") // public client 경우 사용
                    .authorizedGrantTypes("authorization_code") // Browser SPA Client 경우 사용
//                    .authorizedGrantTypes("authorization_code,refresh_token") // Mobile Client 경우 사용
                    .scope("read,write")
                    .authorities("user")
                    .resourceIds("auth")
                    .webServerRedirectUri("http://localhost:9600")
                    .autoApprove("true")
                    .build();
            entityManager.persist(client);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
