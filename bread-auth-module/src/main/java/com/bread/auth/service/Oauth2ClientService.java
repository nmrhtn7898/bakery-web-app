package com.bread.auth.service;

import com.bread.auth.entity.Oauth2Client;
import com.bread.auth.model.Oauth2ClientDetails;
import com.bread.auth.repository.Oauth2ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Oauth2ClientService implements ClientDetailsService {

    private final Oauth2ClientRepository oauth2ClientRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Oauth2Client client = oauth2ClientRepository
                .findByClientId(clientId)
                .orElseThrow(() -> new NoSuchClientException(clientId));
        return new Oauth2ClientDetails(client);
    }
}
