package com.bakery.auth.config.custom;

import com.bakery.auth.enums.CodeChallengeMethod;
import com.bakery.auth.model.AccountDetails;
import com.bakery.auth.model.Oauth2CodeRequest;
import com.bakery.auth.repository.Oauth2CodeRequestRedisRepository;
import com.bakery.auth.service.Oauth2ClientService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class PkceAuthorizationCodeService implements AuthorizationCodeServices {

    private final RandomValueStringGenerator generator = new RandomValueStringGenerator();

    private final Oauth2CodeRequestRedisRepository oauth2CodeRequestRedisRepository;

    private final Oauth2ClientService clientDetailsService;

    private final PasswordEncoder passwordEncoder;

    /**
     * authorization_code 방식 code 생성 메소드, loadByClientId 이후 진입
     *
     * @param authentication 클라이언트 정보 및 Oauth2 인증 파라미터 정보
     * @return
     */
    @SneakyThrows
    @Override
    public String createAuthorizationCode(OAuth2Authentication authentication) {
        Oauth2CodeRequest oauth2CodeRequest = getProtectedAuthentication(authentication);
        oauth2CodeRequestRedisRepository.save(oauth2CodeRequest);
        return oauth2CodeRequest.getCode();
    }

    /**
     * 기존 AuthorizationCodeTokenGranter -> 토큰 발급시 호출하던 메소드, granter 별도 구현하여 사용하지 않음
     *
     * @param code authorization_code
     * @return
     */
    @Override
    public OAuth2Authentication consumeAuthorizationCode(String code) {
        throw new UnsupportedOperationException();
    }

    /**
     * code 값으로 authorization_code 발급시 code 를 key 값으로 저장한 code_challenge 값과 code_verifier 암호화 후 비교
     *
     * @param code     authorization_code
     * @param verifier code_verifier
     * @return
     */
    @SneakyThrows
    public OAuth2Authentication consumeAuthorizationCodeAndCodeVerifier(String code, String verifier) {
        return oauth2CodeRequestRedisRepository
                .findById(code)
                .orElseThrow(() -> new InvalidGrantException("invalid authorization code"))
                .getAuth2Authentication(verifier);
    }

    /**
     * 토큰 발급 후, 저장한 인증 객체 제거
     *
     * @param code authorization_code
     */
    public void removeStoredAuthentication(String code) {
        oauth2CodeRequestRedisRepository.deleteById(code);
    }

    /**
     * @param oAuth2Authentication 클라이언트 정보 및 Oauth2 인증 파라미터 정보
     * @return
     */
    private Oauth2CodeRequest getProtectedAuthentication(OAuth2Authentication oAuth2Authentication) {
        Map<String, String> requestParameters = oAuth2Authentication
                .getOAuth2Request()
                .getRequestParameters();
        if (!requestParameters.containsKey("code_challenge")) {
            throw new InvalidRequestException("code challenge required.");
        }
        String codeChallenge = requestParameters.get("code_challenge");
        CodeChallengeMethod codeChallengeMethod = getCodeChallengeMethod(requestParameters);
        return new Oauth2CodeRequest(
                generator.generate(),
                codeChallenge,
                codeChallengeMethod,
                oAuth2Authentication.getOAuth2Request(),
                (AccountDetails) oAuth2Authentication
                        .getUserAuthentication()
                        .getPrincipal()
        );
    }


    private CodeChallengeMethod getCodeChallengeMethod(Map<String, String> requestParameters) {
        try {
            return ofNullable(requestParameters.get("code_challenge_method"))
                    .map(String::toUpperCase)
                    .map(CodeChallengeMethod::valueOf)
                    .orElse(CodeChallengeMethod.S256);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("transform algorithm not supported");
        }
    }

    /**
     * 해당 클라이언트가 시크릿을 가진 클라이언트인지 검사
     *
     * @param clientId 클라이언트 ID
     * @return
     */
    private boolean isPublicClient(String clientId) {
        String clientSecret = clientDetailsService
                .loadClientByClientId(clientId)
                .getClientSecret();
        return clientSecret == null || passwordEncoder.matches("", clientSecret);
    }

}
