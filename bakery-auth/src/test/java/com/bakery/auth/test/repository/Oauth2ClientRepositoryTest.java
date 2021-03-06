package com.bakery.auth.test.repository;

import com.bakery.auth.entity.Oauth2Client;
import com.bakery.auth.base.AbstractDataJpaTest;
import com.bakery.auth.repository.Oauth2ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.NoSuchClientException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("Oauth2ClientRepository 단위 테스트")
public class Oauth2ClientRepositoryTest extends AbstractDataJpaTest {

    @Autowired
    private Oauth2ClientRepository oauth2ClientRepository;

    @Test
    @DisplayName("clientId 기준으로 조회 성공하는 경우")
    public void findByClientId_Success() {
        // given
        String clientId = "clientId";
        Oauth2Client expect = generate(clientId);
        expect = oauth2ClientRepository.save(expect);
        // when
        Oauth2Client actual = oauth2ClientRepository
                .findByClientId(clientId)
                .orElseThrow(() -> new NoSuchClientException(clientId));
        // then
        assertEquals(expect.getId(), actual.getId());
        assertEquals(expect.getClientId(), actual.getClientId());
        assertEquals(expect.getClientSecret(), actual.getClientSecret());
        assertEquals(expect.getScope(), actual.getScope());
        assertEquals(expect.getResourceIds(), actual.getResourceIds());
        assertEquals(expect.getAuthorizedGrantTypes(), actual.getAuthorizedGrantTypes());
        assertEquals(expect.getWebServerRedirectUri(), actual.getWebServerRedirectUri());
        assertEquals(expect.getAuthorities(), actual.getAuthorities());
        assertEquals(expect.getAccessTokenValidity(), actual.getAccessTokenValidity());
        assertEquals(expect.getRefreshTokenValidity(), actual.getRefreshTokenValidity());
        assertEquals(expect.getAdditionalInformation(), actual.getAdditionalInformation());
        assertEquals(expect.getAutoApprove(), actual.getAutoApprove());
    }

    @Test
    @DisplayName("clientId 기준으로 조회 실패하는 경우")
    public void findByClientId_Fail() {
        // given
        String clientId = "not exists client id";
        // when & then
        assertFalse(oauth2ClientRepository.findByClientId(clientId).isPresent());
    }

    private Oauth2Client generate(String clientId) {
        return Oauth2Client
                .builder()
                .clientId(clientId)
                .clientSecret("clientSecret")
                .resourceIds("resource")
                .scope("read,write")
                .authorizedGrantTypes("password,refresh_token")
                .authorities("user")
                .webServerRedirectUri("/")
                .build();
    }

}
