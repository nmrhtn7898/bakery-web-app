package com.bread.auth.config.custom;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.RedirectMismatchException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.HashMap;
import java.util.Map;

public class PkceAuthorizationCodeTokenGranter extends AuthorizationCodeTokenGranter {

    private final PkceAuthorizationCodeService pkceAuthorizationCodeService;

    public PkceAuthorizationCodeTokenGranter(AuthorizationServerTokenServices tokenServices, PkceAuthorizationCodeService pkceAuthorizationCodeService,
                                             ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        super(tokenServices, pkceAuthorizationCodeService, clientDetailsService, requestFactory);
        this.pkceAuthorizationCodeService = pkceAuthorizationCodeService;
    }

    /**
     * code 발급 후, 토큰 교환 엔드포인트 접근 시
     *
     * @param client       클라이언트 정보
     * @param tokenRequest 토큰 요청 파라미터
     * @return
     */
    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = tokenRequest.getRequestParameters();
        String authorizationCode = parameters.get("code");
        String redirectUri = parameters.get("redirect_uri");
        if (authorizationCode == null) {
            throw new InvalidRequestException("an authorization code must be supplied.");
        } else {
            String codeVerifier = parameters.getOrDefault("code_verifier", "");
            OAuth2Authentication oAuth2Authentication = pkceAuthorizationCodeService.consumeAuthorizationCodeAndCodeVerifier(authorizationCode, codeVerifier);
            OAuth2Request oAuth2Request = oAuth2Authentication.getOAuth2Request();
            if (redirectUri == null || !redirectUri.equals(oAuth2Request.getRedirectUri())) {
                throw new RedirectMismatchException("redirect uri mismatch.");
            } else {
                String clientId = tokenRequest.getClientId();
                if (clientId != null && !clientId.equals(oAuth2Request.getClientId())) {
                    throw new InvalidClientException("client id mismatch.");
                } else {
                    Map<String, String> combinedParameters = new HashMap<>(oAuth2Request.getRequestParameters());
                    combinedParameters.putAll(parameters);
                    OAuth2Request finalOauth2Request = oAuth2Request.createOAuth2Request(combinedParameters);
                    Authentication finalAuthentication = oAuth2Authentication.getUserAuthentication();
                    pkceAuthorizationCodeService.removeStoredAuthentication(authorizationCode);
                    return new OAuth2Authentication(finalOauth2Request, finalAuthentication);
                }
            }
        }
    }

}
