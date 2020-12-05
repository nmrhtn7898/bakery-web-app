package com.bakery.auth.test.repository;

import com.bakery.auth.entity.Oauth2Client;
import com.bakery.auth.model.Oauth2ClientDetails;
import com.bakery.auth.repository.Oauth2ClientRedisRepository;
import com.bakery.auth.base.AbstractDataRedisTest;
import javassist.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Oauth2ClientRedisRepository 캐싱 단위 테스트")
public class Oauth2ClientRedisRepositoryTest extends AbstractDataRedisTest {

    @Autowired
    private Oauth2ClientRedisRepository oauth2ClientRedisRepository;

    @Test
    @DisplayName("clientId 기준으로 조회 성공하는 경우")
    public void findById_Success() throws NotFoundException {
        // given
        String clientId = randomUUID().toString();
        Oauth2ClientDetails expect = generate(clientId);
        oauth2ClientRedisRepository.save(expect);
        // when
        Oauth2ClientDetails actual = oauth2ClientRedisRepository
                .findById(clientId)
                .orElseThrow(() -> new NotFoundException(clientId));
        // then
        assertEquals(expect.getId(), actual.getId());
        assertEquals(expect.getClientId(), actual.getClientId());
        assertEquals(expect.getClientSecret(), actual.getClientSecret());
        assertEquals(expect.getResourceIds(), actual.getResourceIds());
        assertEquals(expect.getScope(), actual.getScope());
        assertEquals(expect.getAuthorizedGrantTypes(), actual.getAuthorizedGrantTypes());
        assertEquals(expect.getAuthorities(), actual.getAuthorities());
        assertIterableEquals(expect.getRegisteredRedirectUri(), actual.getRegisteredRedirectUri());
        assertEquals(expect.getAccessTokenValiditySeconds(), actual.getAccessTokenValiditySeconds());
        assertEquals(expect.getRefreshTokenValiditySeconds(), actual.getRefreshTokenValiditySeconds());
        assertEquals(expect.getAdditionalInformation(), actual.getAdditionalInformation());
        assertEquals(expect.getAutoApproveScopes(), actual.getAutoApproveScopes());
    }

    @Test
    @DisplayName("clientId 기준으로 조회 실패하는 경우")
    public void findById_Fail() {
        // given
        String clientId = "not exists client id";
        // when & then
        assertFalse(oauth2ClientRedisRepository.findById(clientId).isPresent());
    }

    @Test
    @DisplayName("clientId 기준으로 삭제 성공하는 경우")
    public void deleteById_Success() {
        // given
        String clientId = randomUUID().toString();
        Oauth2ClientDetails oauth2ClientDetails = generate(clientId);
        oauth2ClientRedisRepository.save(oauth2ClientDetails);
        // when
        oauth2ClientRedisRepository.deleteById(clientId);
        // then
        assertFalse(oauth2ClientRedisRepository.findById(clientId).isPresent());
    }

    private Oauth2ClientDetails generate(String clientId) {
        Oauth2Client oauth2Client = Oauth2Client
                .builder()
                .id(1L)
                .clientId(clientId)
                .clientSecret("clientSecret")
                .resourceIds("resource")
                .scope("read,write")
                .authorizedGrantTypes("password,refresh_token")
                .authorities("user,admin")
                .webServerRedirectUri("/")
                .build();
        return new Oauth2ClientDetails(oauth2Client);
    }

}
