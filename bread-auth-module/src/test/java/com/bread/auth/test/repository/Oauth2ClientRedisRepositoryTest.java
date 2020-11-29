package com.bread.auth.test.repository;

import com.bread.auth.base.AbstractDataRedisTest;
import com.bread.auth.entity.Oauth2Client;
import com.bread.auth.model.Oauth2ClientDetails;
import com.bread.auth.repository.Oauth2ClientRedisRepository;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class Oauth2ClientRedisRepositoryTest extends AbstractDataRedisTest {

    @Autowired
    private Oauth2ClientRedisRepository oauth2ClientRedisRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void beforeEach() {
        oauth2ClientRedisRepository.deleteAll();
    }

    @Test
    public void save_Success() throws NotFoundException {
        // given
        String clientId = "generate";
        String clientSecret = "secret";
        String resourceIds = "auth";
        String scopes = "read,write";
        String grantTypes = "password,refresh_token";
        String authorities = "user";
        String redirectUri = "/";
        int tokenValidity = 1800;
        Oauth2Client oauth2Client = Oauth2Client
                .builder()
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(clientSecret))
                .resourceIds(resourceIds)
                .scope(scopes)
                .authorizedGrantTypes(grantTypes)
                .authorities(authorities)
                .webServerRedirectUri(redirectUri)
                .accessTokenValidity(tokenValidity)
                .refreshTokenValidity(tokenValidity)
                .build();
        Oauth2ClientDetails expect = new Oauth2ClientDetails(oauth2Client);
        // when
        Oauth2ClientDetails save = oauth2ClientRedisRepository.save(expect);
        Oauth2ClientDetails find = oauth2ClientRedisRepository
                .findById(clientId)
                .orElseThrow(() -> new NotFoundException(clientId));
        // then
        assertEquals(save.getClientId(), find.getClientId());
        assertEquals(save.getClientSecret(), find.getClientSecret());
        assertEquals(save.getResourceIds(), find.getResourceIds());
        assertEquals(save.getScope(), find.getScope());
        assertEquals(save.getAuthorizedGrantTypes(), find.getAuthorizedGrantTypes());
        assertEquals(save.getAuthorities(), find.getAuthorities());
        assertIterableEquals(save.getRegisteredRedirectUri(), find.getRegisteredRedirectUri());
        assertEquals(save.getAccessTokenValiditySeconds(), find.getAccessTokenValiditySeconds());
        assertEquals(save.getRefreshTokenValiditySeconds(), find.getRefreshTokenValiditySeconds());
    }

}
