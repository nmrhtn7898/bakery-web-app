package com.bread.auth.config;

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
            throw new InvalidGrantException("Invalid authorization code");
        }
        return authentication.getAuth2Authentication(verifier);
    }

    /**
     * 토큰 발급 후, 저장한 인증 객체 제거
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
        // 해당 클라이언트 정보가 시크릿 값이 없는 경우, code_challenge 파라미터가 없으면 예외 발생
        if (isPublicClient(requestParameters.get("client_id")) && !requestParameters.containsKey("code_challenge")) {
            throw new InvalidRequestException("Code challenge required.");
        }
        if (requestParameters.containsKey("code_challenge")) {
            String codeChallenge = requestParameters.get("code_challenge");
            CodeChallengeMethod codeChallengeMethod = getCodeChallengeMethod(requestParameters);
            return new PkceAuthentication(codeChallenge, codeChallengeMethod, oAuth2Authentication);
        }
        return new PkceAuthentication(oAuth2Authentication);
    }

    private CodeChallengeMethod getCodeChallengeMethod(Map<String, String> requestParameters) {
        try {
            return ofNullable(requestParameters.get("code_challenge_method"))
                    .map(String::toUpperCase)
                    .map(CodeChallengeMethod::valueOf)
                    .orElse(CodeChallengeMethod.PLAIN);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Transform algorithm not supported");
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
