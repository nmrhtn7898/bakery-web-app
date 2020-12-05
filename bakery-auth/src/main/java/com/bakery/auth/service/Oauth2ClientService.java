package com.bakery.auth.service;

import com.bakery.auth.entity.Oauth2Client;
import com.bakery.auth.model.Oauth2ClientDetails;
import com.bakery.auth.repository.Oauth2ClientRedisRepository;
import com.bakery.auth.repository.Oauth2ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class Oauth2ClientService implements ClientDetailsService {

    private final Oauth2ClientRepository oauth2ClientRepository;

    private final Oauth2ClientRedisRepository oauth2ClientRedisRepository;

    @Transactional(readOnly = true)
    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Optional<Oauth2ClientDetails> byId = oauth2ClientRedisRepository.findById(clientId);
        if (byId.isPresent()) {
            log.debug("client id : {} is caching", clientId);
            return byId.get();
        }
        Oauth2Client client = oauth2ClientRepository
                .findByClientId(clientId)
                .orElseThrow(() -> new NoSuchClientException(clientId));
        Oauth2ClientDetails oauth2ClientDetails = new Oauth2ClientDetails(client);
        log.debug("client id : {} has been cached", clientId);
        return oauth2ClientRedisRepository.save(oauth2ClientDetails);
    }

}
