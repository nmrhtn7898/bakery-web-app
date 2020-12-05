package com.bakery.auth.test.service;

import com.bakery.auth.entity.Oauth2Client;
import com.bakery.auth.model.Oauth2ClientDetails;
import com.bakery.auth.repository.Oauth2ClientRedisRepository;
import com.bakery.auth.base.AbstractServiceTest;
import com.bakery.auth.repository.Oauth2ClientRepository;
import com.bakery.auth.service.Oauth2ClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.oauth2.provider.NoSuchClientException;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Oauth2ClientService 단위 테스트")
public class Oauth2ClientServiceTest extends AbstractServiceTest {

    @InjectMocks
    private Oauth2ClientService oauth2ClientService;

    @Mock
    private Oauth2ClientRepository oauth2ClientRepository;

    @Mock
    private Oauth2ClientRedisRepository oauth2ClientRedisRepository;

    @Test
    @DisplayName("clientId 기준으로 조회 성공하는 경우")
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
    @DisplayName("clientId 기준으로 캐시 조회 성공 하는 경우")
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
    @DisplayName("clientId 기준으로 조회 실패하는 경우")
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
