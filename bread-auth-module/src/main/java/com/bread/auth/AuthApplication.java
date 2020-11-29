package com.bread.auth;

import com.bread.auth.entity.Account;
import com.bread.auth.entity.AccountAuthority;
import com.bread.auth.entity.Authority;
import com.bread.auth.entity.Oauth2Client;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static com.bread.auth.enums.Oauth2GrantType.*;
import static java.lang.String.format;
import static org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder;

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
//                    .authorizedGrantTypes(AUTHORIZATION_CODE.toString()) // Browser SPA Client SSO 경우 사용
//                    .authorizedGrantTypes(  // Mobile Client SSO 경우 사용
//                            format(
//                                    "%s,%s",
//                                    AUTHORIZATION_CODE.toString(),
//                                    REFRESH_TOKEN.toString()
//                            )
//                    )
                    .authorizedGrantTypes(
                            format(
                                    "%s,%s,%s",
                                    AUTHORIZATION_CODE.toString(),
                                    PASSWORD.toString(),
                                    REFRESH_TOKEN.toString()
                            )
                    )
                    .scope("read,write")
                    .authorities("user")
                    .resourceIds("auth")
                    .webServerRedirectUri("http://localhost:9600/auth")
                    .autoApprove("true")
                    .build();
            entityManager.persist(client);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
