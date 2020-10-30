package com.bread.auth;

import com.bread.auth.base.AbstractDataJpaTest;
import com.bread.auth.entity.Oauth2Client;
import com.bread.auth.repository.Oauth2ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.NoSuchClientException;

import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class Oauth2ClientRepositoryTestAbstract extends AbstractDataJpaTest {

    @Autowired
    private Oauth2ClientRepository oauth2ClientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void findByClientId_Success() {
        String clientId = "test";
        Oauth2Client client = oauth2ClientRepository
                .findByClientId(clientId)
                .orElseThrow(() -> new NoSuchClientException(clientId));
        assertEquals(client.getClientId(), clientId);
        assertTrue(passwordEncoder.matches("1234", client.getClientSecret()));
        assertEquals(client.getAuthorizedGrantTypes(), "password,refresh_token");
        assertEquals(client.getScope(), "read,write");
        assertEquals(client.getAuthorities(), "user");
        assertEquals(client.getResourceIds(), "auth");
        assertEquals(client.getWebServerRedirectUri(), "http://localhost:9600");
    }

    @Test
    public void findByClientId_Fail() {
        String clientId = "invalid client Id";
        assertThrows(NoSuchClientException.class, () ->
                oauth2ClientRepository
                        .findByClientId(clientId)
                        .orElseThrow(() -> new NoSuchClientException(clientId))
        );
    }

}
