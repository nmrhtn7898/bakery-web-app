package com.bread.auth.config.custom;

import com.bread.auth.model.RememberMeCaching;
import com.bread.auth.repository.RememberMeRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.PartialUpdate;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomRememberMeTokenRepository implements PersistentTokenRepository {

    private final RememberMeRedisRepository rememberMeRedisRepository;

    private final RedisKeyValueTemplate redisKeyValueTemplate;

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        RememberMeCaching rememberMeCaching = new RememberMeCaching(token);
        rememberMeRedisRepository.save(rememberMeCaching);
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        log.info("series id : {} remember-me token is updated", series);
        PartialUpdate<RememberMeCaching> update = new PartialUpdate<>(series, RememberMeCaching.class)
                .set("token", tokenValue)
                .set("lastUsed", lastUsed);
        redisKeyValueTemplate.update(update);
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        Optional<RememberMeCaching> byId = rememberMeRedisRepository.findById(seriesId);
        if (byId.isPresent()) {
            log.info("series id : {} remember-me token has been cached", seriesId);
            RememberMeCaching rememberMeCaching = byId.get();
            return new PersistentRememberMeToken(
                    rememberMeCaching.getEmail(),
                    rememberMeCaching.getSeries(),
                    rememberMeCaching.getToken(),
                    rememberMeCaching.getLastUsed()
            );
        } else {
            return null;
        }
    }

    @Override
    public void removeUserTokens(String username) {
        rememberMeRedisRepository
                .findByEmail(username)
                .ifPresent(rememberMeCaching -> {
                    log.info("username : {} remember-me token is deleted", username);
                    rememberMeRedisRepository.delete(rememberMeCaching);
                });
    }

}
