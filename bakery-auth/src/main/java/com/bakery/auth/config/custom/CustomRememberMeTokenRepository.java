package com.bakery.auth.config.custom;

import com.bakery.auth.model.RememberMeToken;
import com.bakery.auth.repository.RememberMeTokenRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomRememberMeTokenRepository implements PersistentTokenRepository {

    private final RememberMeTokenRedisRepository rememberMeTokenRedisRepository;

    /**
     * remember-me 토큰 생성하고 캐시한다.
     *
     * @param token remember-me 토큰
     */
    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        log.debug("username : {} remember me logged in", token.getUsername());
        RememberMeToken rememberMeToken = new RememberMeToken(token);
        rememberMeTokenRedisRepository.save(rememberMeToken);
    }

    /**
     * remember-me 토큰을 사용해서 로그인을 수행한 경우 remember-me 토큰의 tokenValue, lastUser 값을 변경한다.
     * 토큰 쿠키의 값은 series, tokenValue 값을 인코딩한 값인데 series 부분은 유지되고 tokenValue 부분을 변경하여 쿠키를 변경한다.
     *
     * @param series     remember-me 토큰 식별키
     * @param tokenValue remember-me 토큰 값
     * @param lastUsed   remember-me 토큰 최근 사용일
     */
    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        log.debug("series id : {} remember-me token was used", series);
        rememberMeTokenRedisRepository.updateTokenAndLastUsed(series, tokenValue, lastUsed);
    }

    /**
     * remember-me 쿠키로 토큰을 조회한다.
     *
     * @param seriesId remember-me 토큰 식별키
     * @return
     */
    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        Optional<RememberMeToken> byId = rememberMeTokenRedisRepository.findById(seriesId);
        if (byId.isPresent()) {
            log.debug("series id : {} remember-me token is caching", seriesId);
            RememberMeToken rememberMeToken = byId.get();
            return new PersistentRememberMeToken(
                    rememberMeToken.getEmail(),
                    rememberMeToken.getSeries(),
                    rememberMeToken.getToken(),
                    rememberMeToken.getLastUsed()
            );
        } else {
            return null;
        }
    }

    /**
     * remember-me 토큰 사용 시, 토큰의 series 부분은 동일하나 tokenValue 부분이 다른 경우
     * 토큰 탈취 또는 위조 공격으로 간주하여 해당 계정의 remember-me 토큰을 모두 제거한다.
     *
     * @param username 계정 이름
     */
    @Override
    public void removeUserTokens(String username) {
        log.warn("username : {} delete all remember-me token", username);
        rememberMeTokenRedisRepository.deleteAllByEmail(username);
    }

    /**
     * 로그아웃 시 remember-me 쿠키 및 토큰 제거
     *
     * @param series remember-me 토큰 식별키
     */
    public void removeUserToken(String series) {
        log.debug("series id : {} delete remember-me token", series);
        rememberMeTokenRedisRepository.deleteById(series);
    }

}
