package com.bakery.auth.repository.custom;

import com.bakery.auth.model.RememberMeToken;
import org.springframework.data.redis.core.PartialUpdate;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.index.Indexed;

import java.util.Date;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

public class RememberMeTokenRedisRepositoryImpl implements RememberMeTokenRedisRepositoryCustom {

    private final RedisTemplate<String, Object> redisTemplate;

    private final RedisKeyValueTemplate redisKeyValueTemplate;

    private final String hashKey;

    private final String indexFieldName;

    public RememberMeTokenRedisRepositoryImpl(RedisKeyValueTemplate redisKeyValueTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.redisKeyValueTemplate = redisKeyValueTemplate;
        this.redisTemplate = redisTemplate;
        this.hashKey = RememberMeToken.class.getAnnotation(RedisHash.class).value();
        this.indexFieldName = stream(RememberMeToken.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Indexed.class))
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .getName();
    }

    @Override
    public void deleteAllByEmail(String email) {
        String setKey = format("%s:%s:%s", hashKey, indexFieldName, email);
        requireNonNull(
                redisTemplate
                        .opsForSet()
                        .members(setKey)
        )
                .forEach(data -> {
                    String hashKey = format("%s:%s", this.hashKey, data);
                    redisTemplate.delete(hashKey);
                    redisTemplate.delete(format("%s:%s", hashKey, "idx"));
                    redisTemplate.delete(format("%s:%s", hashKey, "phantom"));
                });
        redisTemplate.delete(setKey);
    }

    @Override
    public void updateTokenAndLastUsed(String series, String tokenValue, Date lastUsed) {
        PartialUpdate<RememberMeToken> update = new PartialUpdate<>(series, RememberMeToken.class)
                .set("token", tokenValue)
                .set("lastUsed", lastUsed);
        redisKeyValueTemplate.update(update);
    }

}
