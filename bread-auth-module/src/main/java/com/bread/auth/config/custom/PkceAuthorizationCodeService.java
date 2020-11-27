package com.bread.auth.config.custom;

import com.bread.auth.enums.CodeChallengeMethod;
import com.bread.auth.service.Oauth2ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class PkceAuthorizationCodeService implements AuthorizationCodeServices {

    private final RandomValueStringGenerator generator = new RandomValueStringGenerator();

    // TODO 인메모리 아닌, redis 캐싱하려 했으나, Oauth2Authentication 클래스 필드, 상위 클래스가 직렬화 불가능
    // TODO code 발급하고, token 발급 안한 code 처리 필요 DB 사용하면 되는데 다른 방법있는지 고려
    private final Map<String, PkceAuthentication> authenticationMap = new ConcurrentHashMap<>();

    private final Oauth2ClientService clientDetailsService;

    private final PasswordEncoder passwordEncoder;

    /**
     * authorization_code 방식 code 생성 메소드, loadByClientId 이후 진입
     *
     * @param authentication 클라이언트 정보 및 Oauth2 인증 파라미터 정보
     * @return
     */
    @Override
    public String createAuthorizationCode(OAuth2Authentication authentication) {
        PkceAuthentication PKCEAuthentication = getProtectedAuthentication(authentication);
        String code = generator.generate();
        authenticationMap.put(code, PKCEAuthentication);
        return code;
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
    public OAuth2Authentication consumeAuthorizationCodeAndCodeVerifier(String code, String verifier) {
        PkceAuthentication authentication = authenticationMap.get(code);
        if (authentication == null) {
            throw new InvalidGrantException("invalid authorization code");
        }
        return authentication.getAuth2Authentication(verifier);
    }

    /**
     * 토큰 발급 후, 저장한 인증 객체 제거
     *
     * @param code authorization_code
     */
    public void removeStoredAuthentication(String code) {
        authenticationMap.remove(code);
    }

    /**
     * @param oAuth2Authentication 클라이언트 정보 및 Oauth2 인증 파라미터 정보
     * @return
     */
    private PkceAuthentication getProtectedAuthentication(OAuth2Authentication oAuth2Authentication) {
        Map<String, String> requestParameters = oAuth2Authentication
                .getOAuth2Request()
                .getRequestParameters();
        if (!requestParameters.containsKey("code_challenge")) {
            throw new InvalidRequestException("code challenge required.");
        }
        String codeChallenge = requestParameters.get("code_challenge");
        CodeChallengeMethod codeChallengeMethod = getCodeChallengeMethod(requestParameters);
        return new PkceAuthentication(codeChallenge, codeChallengeMethod, oAuth2Authentication);
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
