package com.bakery.auth.config;

import com.bakery.auth.entity.Account;
import com.bakery.auth.entity.AccountAuthority;
import com.bakery.auth.entity.Authority;
import com.bakery.auth.entity.Oauth2Client;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@TestConfiguration
public class TestDataConfig {

    @Component
    @Getter
    @Setter
    @ConfigurationProperties(prefix = "test")
    public static class TestProperties {

        public Users users;

        public Clients clients;

        @Getter
        @Setter
        public static class Users {
            private User noAuthorities;
            private User master;
        }

        @Getter
        @Setter
        public static class User {
            private String username;
            private String password;
            private List<String> authorities;
        }

        @Getter
        @Setter
        public static class Clients {
            private Client master;
            private Client noScopes;
            private Client noGrantTypes;
        }

        @Getter
        @Setter
        public static class Client {
            private String clientId;
            private String clientSecret;
            private String redirectUris;
            private String grantTypes;
            private String scopes;
            private String authorities;
            private String resourceIds;
            private String codeChallenge;
            private String codeChallengeMethod;
            private String codeVerifier;
            private String state;
        }

    }


    @Component
    @RequiredArgsConstructor
    public static class DataInitRunner implements ApplicationRunner {
        private final EntityManager entityManager;
        private final PasswordEncoder passwordEncoder;
        private final TestProperties testProperties;

        @Transactional
        @Override
        public void run(ApplicationArguments args) throws Exception {
            Authority userAuthority = Authority
                    .builder()
                    .name("user")
                    .build();
            Authority adminAuthority = Authority
                    .builder()
                    .name("admin")
                    .build();
            Account noAuthoritiesUser = Account
                    .builder()
                    .email(testProperties.getUsers().getNoAuthorities().getUsername())
                    .password(passwordEncoder.encode(testProperties.getUsers().getNoAuthorities().getPassword()))
                    .build();
            Account masterUser = Account
                    .builder()
                    .email(testProperties.getUsers().getMaster().getUsername())
                    .password(passwordEncoder.encode(testProperties.getUsers().getMaster().getPassword()))
                    .build();
            AccountAuthority masterAuthority = AccountAuthority
                    .builder()
                    .account(masterUser)
                    .authority(userAuthority)
                    .build();
            AccountAuthority masterAuthority2 = AccountAuthority
                    .builder()
                    .account(masterUser)
                    .authority(adminAuthority)
                    .build();
            entityManager.persist(userAuthority);
            entityManager.persist(adminAuthority);
            entityManager.persist(noAuthoritiesUser);
            entityManager.persist(masterUser);
            entityManager.persist(masterAuthority);
            entityManager.persist(masterAuthority2);

            Oauth2Client masterClient = Oauth2Client
                    .builder()
                    .clientId(testProperties.getClients().getMaster().getClientId())
                    .clientSecret(passwordEncoder.encode(testProperties.getClients().getMaster().getClientSecret()))
                    .authorizedGrantTypes(testProperties.getClients().getMaster().getGrantTypes())
                    .scope(testProperties.getClients().getMaster().getScopes())
                    .authorities(testProperties.getClients().getMaster().getAuthorities())
                    .resourceIds(testProperties.getClients().getMaster().getResourceIds())
                    .webServerRedirectUri(testProperties.getClients().getMaster().getRedirectUris())
                    .build();
            Oauth2Client noGrantTypesClient = Oauth2Client
                    .builder()
                    .clientId(testProperties.getClients().getNoGrantTypes().getClientId())
                    .clientSecret(passwordEncoder.encode(testProperties.getClients().getNoGrantTypes().getClientSecret()))
                    .authorizedGrantTypes(testProperties.getClients().getNoGrantTypes().getGrantTypes())
                    .scope(testProperties.getClients().getNoGrantTypes().getScopes())
                    .authorities(testProperties.getClients().getNoGrantTypes().getAuthorities())
                    .resourceIds(testProperties.getClients().getNoGrantTypes().getResourceIds())
                    .webServerRedirectUri(testProperties.getClients().getNoGrantTypes().getRedirectUris())
                    .build();
            Oauth2Client noScopesClient = Oauth2Client
                    .builder()
                    .clientId(testProperties.getClients().getNoScopes().getClientId())
                    .clientSecret(passwordEncoder.encode(testProperties.getClients().getNoScopes().getClientSecret()))
                    .authorizedGrantTypes(testProperties.getClients().getNoScopes().getGrantTypes())
                    .scope(testProperties.getClients().getNoScopes().getScopes())
                    .authorities(testProperties.getClients().getNoScopes().getAuthorities())
                    .resourceIds(testProperties.getClients().getNoScopes().getResourceIds())
                    .webServerRedirectUri(testProperties.getClients().getNoScopes().getRedirectUris())
                    .build();
            entityManager.persist(masterClient);
            entityManager.persist(noGrantTypesClient);
            entityManager.persist(noScopesClient);
        }
    }

}
