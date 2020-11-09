package com.bread.auth.service;

import com.bread.auth.entity.Oauth2Client;
import com.bread.auth.model.Oauth2ClientCaching;
import com.bread.auth.repository.Oauth2ClientRepository;
import com.bread.auth.repository.redis.Oauth2ClientRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static java.util.Arrays.asList;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class Oauth2ClientService implements ClientDetailsService {

    private final Oauth2ClientRepository oauth2ClientRepository;

    private final Oauth2ClientRedisRepository oauth2ClientRedisRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Optional<Oauth2ClientCaching> byId = oauth2ClientRedisRepository.findById(clientId);
        if (byId.isPresent()) {
            log.info("client id : {} is caching", clientId);
            return byId.get();
        }
        Oauth2Client client = oauth2ClientRepository
                .findByClientId(clientId)
                .orElseThrow(() -> new NoSuchClientException(clientId));
        Oauth2ClientCaching oauth2ClientCaching = new Oauth2ClientCaching(client);
        log.info("client id : {} has been cached", clientId);
        return oauth2ClientRedisRepository.save(oauth2ClientCaching);
    }

    public Oauth2Client generateClient(Oauth2Client oauth2Client) {
        oauth2Client.setClientSecret(passwordEncoder.encode(oauth2Client.getClientSecret()));
        return oauth2ClientRepository.save(oauth2Client);
    }

}
