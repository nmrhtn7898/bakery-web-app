package com.bread.auth;

import com.bread.auth.entity.Account;
import com.bread.auth.entity.AccountAuthority;
import com.bread.auth.entity.Authority;
import com.bread.auth.entity.Oauth2Client;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.File;
import java.util.Arrays;

@EnableJpaAuditing
@EnableRedisRepositories
@SpringBootApplication
public class AuthApplication {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey("temp_secret");
        return jwtAccessTokenConverter;
    }

    @Bean
    @Profile("!test")
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

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

    @Profile(value = {"dev", "prod"})
    @Bean
    public StringEncryptor stringEncryptor(@Value("${encrypt.key}") String key,
                                           @Value("${encrypt.alg}") String algorithm) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(key);
        config.setAlgorithm(algorithm);
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }

/*    @Profile("default")
    @Bean
    public ApplicationRunner directoryInitRunner() {
        return (args) -> {
            File databaseDirectory = new File("../docker/default/database");
            File redisDirectory = new File("../docker/default/redis");
            if (!databaseDirectory.exists()) {
                databaseDirectory.mkdir();
            }
            if (!redisDirectory.exists()) {
                databaseDirectory.mkdir();
            }
        };
    }*/

    @Profile(value = {"default", "test", "dev"})
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
                    .clientSecret(passwordEncoder.encode("1234"))
                    .authorizedGrantTypes("password,refresh_token")
                    .scope("read,write")
                    .authorities("user")
                    .resourceIds("auth")
                    .webServerRedirectUri("http://localhost:9600")
                    .build();
            entityManager.persist(client);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
