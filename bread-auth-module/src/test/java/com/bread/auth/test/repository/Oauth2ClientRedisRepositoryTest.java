package com.bread.auth.test.repository;

import com.bread.auth.base.AbstractDataRedisTest;
import com.bread.auth.entity.Oauth2Client;
import com.bread.auth.model.Oauth2ClientDetails;
import com.bread.auth.repository.Oauth2ClientRedisRepository;
import javassist.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

public class Oauth2ClientRedisRepositoryTest extends AbstractDataRedisTest {

    @Autowired
    private Oauth2ClientRedisRepository oauth2ClientRedisRepository;

    @Test
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
    public void findById_Fail() {
        // given
        String clientId = "not exists client id";
        // when & then
        assertFalse(oauth2ClientRedisRepository.findById(clientId).isPresent());
    }

    @Test
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
