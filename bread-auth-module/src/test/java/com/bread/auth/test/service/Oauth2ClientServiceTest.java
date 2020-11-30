package com.bread.auth.test.service;

import com.bread.auth.base.AbstractServiceTest;
import com.bread.auth.entity.Oauth2Client;
import com.bread.auth.model.Oauth2ClientDetails;
import com.bread.auth.repository.Oauth2ClientRedisRepository;
import com.bread.auth.repository.Oauth2ClientRepository;
import com.bread.auth.service.Oauth2ClientService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.oauth2.provider.NoSuchClientException;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public class Oauth2ClientServiceTest extends AbstractServiceTest {

    @InjectMocks
    private Oauth2ClientService oauth2ClientService;

    @Mock
    private Oauth2ClientRepository oauth2ClientRepository;

    @Mock
    private Oauth2ClientRedisRepository oauth2ClientRedisRepository;

    @Test
    public void loadClientByClientId_Success() {
        // given
        String clientId = "clientId";
        Oauth2Client oauth2Client = generate(clientId);
        Oauth2ClientDetails expect = new Oauth2ClientDetails(oauth2Client);
        when(oauth2ClientRedisRepository.findById(clientId)).thenReturn(empty());
        when(oauth2ClientRepository.findByClientId(clientId)).thenReturn(of(oauth2Client));
        when(oauth2ClientRedisRepository.save(any())).thenReturn(expect);
        // when
        Oauth2ClientDetails actual = (Oauth2ClientDetails) oauth2ClientService.loadClientByClientId(clientId);
        // then
        assertEquals(expect.getClientId(), actual.getClientId());
        assertEquals(expect.getClientSecret(), actual.getClientSecret());
        assertEquals(expect.getAccessTokenValiditySeconds(), actual.getAccessTokenValiditySeconds());
        assertEquals(expect.getRefreshTokenValiditySeconds(), actual.getRefreshTokenValiditySeconds());
        assertEquals(expect.getAdditionalInformation(), actual.getAdditionalInformation());
        assertIterableEquals(expect.getResourceIds(), actual.getResourceIds());
        assertIterableEquals(expect.getScope(), actual.getScope());
        assertIterableEquals(expect.getAuthorizedGrantTypes(), actual.getAuthorizedGrantTypes());
        assertIterableEquals(expect.getAuthorities(), actual.getAuthorities());
        assertIterableEquals(expect.getRegisteredRedirectUri(), actual.getRegisteredRedirectUri());
        assertIterableEquals(expect.getAutoApproveScopes(), actual.getAutoApproveScopes());
    }

    @Test
    public void loadClientByClientIdCache_Success() {
        // given
        String clientId = "clientId";
        Oauth2Client oauth2Client = generate(clientId);
        Oauth2ClientDetails expect = new Oauth2ClientDetails(oauth2Client);
        when(oauth2ClientRedisRepository.findById(clientId)).thenReturn(of(expect));
        // when
        Oauth2ClientDetails actual = (Oauth2ClientDetails) oauth2ClientService.loadClientByClientId(clientId);
        // then
        assertEquals(expect.getClientId(), actual.getClientId());
        assertEquals(expect.getClientSecret(), actual.getClientSecret());
        assertEquals(expect.getAccessTokenValiditySeconds(), actual.getAccessTokenValiditySeconds());
        assertEquals(expect.getRefreshTokenValiditySeconds(), actual.getRefreshTokenValiditySeconds());
        assertEquals(expect.getAdditionalInformation(), actual.getAdditionalInformation());
        assertIterableEquals(expect.getResourceIds(), actual.getResourceIds());
        assertIterableEquals(expect.getScope(), actual.getScope());
        assertIterableEquals(expect.getAuthorizedGrantTypes(), actual.getAuthorizedGrantTypes());
        assertIterableEquals(expect.getAuthorities(), actual.getAuthorities());
        assertIterableEquals(expect.getRegisteredRedirectUri(), actual.getRegisteredRedirectUri());
        assertIterableEquals(expect.getAutoApproveScopes(), actual.getAutoApproveScopes());
    }

    @Test
    public void loadClientByClientId_Fail() {
        // given
        String clientId = "mock";
        when(oauth2ClientRedisRepository.findById(clientId)).thenReturn(empty());
        when(oauth2ClientRepository.findByClientId(clientId)).thenReturn(empty());
        // when & then
        assertThrows(
                NoSuchClientException.class,
                () -> oauth2ClientService.loadClientByClientId(clientId)
        );
    }

    private Oauth2Client generate(String clientId) {
        String clientSecret = "secret";
        String resourceIds = "auth";
        String scopes = "read,write";
        String grantTypes = "password,refresh_token";
        String authorities = "user";
        String redirectUri = "/";
        int tokenValidity = 1800;
        return Oauth2Client
                .builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .resourceIds(resourceIds)
                .scope(scopes)
                .authorizedGrantTypes(grantTypes)
                .authorities(authorities)
                .webServerRedirectUri(redirectUri)
                .accessTokenValidity(tokenValidity)
                .refreshTokenValidity(tokenValidity)
                .build();
    }

}
