package com.bread.auth.test.service;

import com.bread.auth.base.AbstractServiceTest;
import com.bread.auth.entity.Oauth2Client;
import com.bread.auth.model.Oauth2ClientCaching;
import com.bread.auth.repository.Oauth2ClientRedisRepository;
import com.bread.auth.repository.Oauth2ClientRepository;
import com.bread.auth.service.Oauth2ClientService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.NoSuchClientException;

import java.util.Optional;

import static java.lang.String.join;
import static java.util.Optional.*;
import static java.util.stream.Collectors.joining;
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

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void loadClientByClientId_Success() {
        // given
        String clientId = "mock";
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
                .clientSecret(clientSecret)
                .resourceIds(resourceIds)
                .scope(scopes)
                .authorizedGrantTypes(grantTypes)
                .authorities(authorities)
                .webServerRedirectUri(redirectUri)
                .accessTokenValidity(tokenValidity)
                .refreshTokenValidity(tokenValidity)
                .build();
        Oauth2ClientCaching expect = new Oauth2ClientCaching(oauth2Client);
        when(oauth2ClientRedisRepository.findById(clientId)).thenReturn(empty());
        when(oauth2ClientRepository.findByClientId(clientId)).thenReturn(of(oauth2Client));
        when(oauth2ClientRedisRepository.save(any())).thenReturn(expect);
        // when
        ClientDetails clientDetails = oauth2ClientService.loadClientByClientId(clientId);
        // then
        assertEquals(clientId, clientDetails.getClientId());
        assertEquals(clientSecret, clientDetails.getClientSecret());
        assertEquals(resourceIds, join(",", clientDetails.getResourceIds()));
        assertEquals(scopes, join(",", clientDetails.getScope()));
        assertEquals(grantTypes, join(",", clientDetails.getAuthorizedGrantTypes()));
        assertEquals(
                authorities,
                clientDetails
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(joining(","))
        );
        assertEquals(redirectUri, join(",", clientDetails.getRegisteredRedirectUri()));
        assertEquals(tokenValidity, clientDetails.getAccessTokenValiditySeconds());
        assertEquals(tokenValidity, clientDetails.getRefreshTokenValiditySeconds());
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

    @Test
    public void generateClient_Success() {
        // given
        String clientId = "mock";
        String clientSecret = "secret";
        String resourceIds = "auth";
        String scopes = "read,write";
        String grantTypes = "password,refresh_token";
        String authorities = "user";
        String redirectUri = "/";
        int tokenValidity = 1800;
        Oauth2Client expect = Oauth2Client
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
        when(passwordEncoder.encode(clientSecret)).thenReturn(clientSecret);
        when(passwordEncoder.matches(clientSecret, clientSecret)).thenReturn(true);
        when(oauth2ClientRepository.save(expect)).thenReturn(expect);
        // when
        Oauth2Client then = oauth2ClientService.generateClient(expect);
        // then
        assertEquals(clientId, then.getClientId());
        assertTrue(passwordEncoder.matches(clientSecret, then.getClientSecret()));
        assertEquals(resourceIds, then.getResourceIds());
        assertEquals(scopes, then.getScope());
        assertEquals(grantTypes, then.getAuthorizedGrantTypes());
        assertEquals(authorities, then.getAuthorities());
        assertEquals(redirectUri, then.getWebServerRedirectUri());
        assertEquals(tokenValidity, then.getAccessTokenValidity());
        assertEquals(tokenValidity, then.getRefreshTokenValidity());
    }

}
