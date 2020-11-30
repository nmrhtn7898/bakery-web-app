package com.bread.auth.test.repository;

import com.bread.auth.base.AbstractDataRedisTest;
import com.bread.auth.model.RememberMeToken;
import com.bread.auth.repository.RememberMeTokenRedisRepository;
import javassist.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import java.util.Date;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

public class RememberMeTokenRedisRepositoryTest extends AbstractDataRedisTest {

    @Autowired
    private RememberMeTokenRedisRepository rememberMeTokenRedisRepository;

    @Test
    public void findBySeries_Success() throws NotFoundException {
        // given
        String series = randomUUID().toString();
        RememberMeToken expect = generate(series);
        expect = rememberMeTokenRedisRepository.save(expect);
        // when
        RememberMeToken actual = rememberMeTokenRedisRepository
                .findById(series)
                .orElseThrow(() -> new NotFoundException(series));
        // then
        assertEquals(expect.getSeries(), actual.getSeries());
        assertEquals(expect.getEmail(), actual.getEmail());
        assertEquals(expect.getToken(), actual.getToken());
        assertEquals(expect.getLastUsed(), actual.getLastUsed());
    }

    @Test
    public void findBySeries_Fail() {
        // given
        String series = "not exists series";
        // when & then
        assertFalse(rememberMeTokenRedisRepository.findById(series).isPresent());
    }

    @Test
    public void deleteById_Success() {
        // given
        String series = randomUUID().toString();
        RememberMeToken rememberMeToken = generate(series);
        rememberMeTokenRedisRepository.save(rememberMeToken);
        // when
        rememberMeTokenRedisRepository.deleteById(series);
        // then
        assertFalse(rememberMeTokenRedisRepository.findById(series).isPresent());
    }

    @Test
    public void deleteAllByEmail_Success() {
        // given
        String series = randomUUID().toString();
        String series2 = randomUUID().toString();
        String series3 = randomUUID().toString();
        String series4 = randomUUID().toString();
        RememberMeToken rememberMeToken = generate(series);
        RememberMeToken rememberMeToken2 = generate(series2);
        RememberMeToken rememberMeToken3 = generate(series3);
        RememberMeToken rememberMeToken4 = generate(series4);
        rememberMeTokenRedisRepository.save(rememberMeToken);
        rememberMeTokenRedisRepository.save(rememberMeToken2);
        rememberMeTokenRedisRepository.save(rememberMeToken3);
        rememberMeTokenRedisRepository.save(rememberMeToken4);
        // when & then
        assertTrue(rememberMeTokenRedisRepository.findById(series).isPresent());
        assertTrue(rememberMeTokenRedisRepository.findById(series2).isPresent());
        assertTrue(rememberMeTokenRedisRepository.findById(series3).isPresent());
        assertTrue(rememberMeTokenRedisRepository.findById(series4).isPresent());
        rememberMeTokenRedisRepository.deleteAllByEmail("user");
        assertFalse(rememberMeTokenRedisRepository.findById(series).isPresent());
        assertFalse(rememberMeTokenRedisRepository.findById(series2).isPresent());
        assertFalse(rememberMeTokenRedisRepository.findById(series3).isPresent());
        assertFalse(rememberMeTokenRedisRepository.findById(series4).isPresent());
    }

    @Test
    public void updateTokenAndLastUsed() throws NotFoundException {
        // given
        String series = randomUUID().toString();
        String token = randomUUID().toString();
        Date lastUsed = new Date(new Date().getTime() + (60 * 60 * 24 * 1000));
        RememberMeToken expect = generate(series);
        expect = rememberMeTokenRedisRepository.save(expect);
        // when
        rememberMeTokenRedisRepository.updateTokenAndLastUsed(series, token, lastUsed);
        RememberMeToken actual = rememberMeTokenRedisRepository
                .findById(series)
                .orElseThrow(() -> new NotFoundException(series));
        // then
        assertNotEquals(expect.getToken(), token);
        assertNotEquals(expect.getLastUsed(), lastUsed);
        assertEquals(expect.getSeries(), actual.getSeries());
        assertEquals(expect.getEmail(), actual.getEmail());
        assertEquals(token, actual.getToken());
        assertEquals(lastUsed, actual.getLastUsed());
    }

    private RememberMeToken generate(String series) {
        PersistentRememberMeToken token = new PersistentRememberMeToken(
                "user",
                series,
                randomUUID().toString(),
                new Date()
        );
        return new RememberMeToken(token);
    }

}
