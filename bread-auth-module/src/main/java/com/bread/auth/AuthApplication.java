package com.bread.auth;

import com.bread.auth.entity.Account;
import com.bread.auth.entity.AccountAuthority;
import com.bread.auth.entity.Oauth2Client;
import com.bread.auth.entity.Authority;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sun.istack.Pool;
import org.apache.catalina.connector.Connector;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.salt.RandomSaltGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@EnableJpaAuditing
@EnableAuthorizationServer
@EnableResourceServer
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
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    public ServletWebServerFactory servletWebServerFactory(@Value("${ajp.protocol}") String protocol,
                                                           @Value("${ajp.port}") int port) {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        Connector connector = new Connector(protocol);
        connector.setPort(port);
        connector.setSecure(false);
        connector.setAllowTrace(false);
        connector.setScheme("http");
        tomcat.addAdditionalTomcatConnectors(connector);
        return tomcat;
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

    @Profile(value = {"default", "test"})
    @Component
    public static class InitRunner implements ApplicationRunner {
        @Autowired
        private EntityManager entityManager;
        @Autowired
        private PasswordEncoder passwordEncoder;

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
