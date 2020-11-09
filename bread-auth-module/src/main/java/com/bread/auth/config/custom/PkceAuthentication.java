package com.bread.auth.config.custom;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

@RequiredArgsConstructor
public class PkceAuthentication {

    private final String codeChallenge;

    private final CodeChallengeMethod codeChallengeMethod;

    private final OAuth2Authentication oAuth2Authentication;

    public PkceAuthentication(OAuth2Authentication oAuth2Authentication) {
        this.codeChallenge = null;
        this.codeChallengeMethod = CodeChallengeMethod.NONE;
        this.oAuth2Authentication = oAuth2Authentication;
    }

    public OAuth2Authentication getAuth2Authentication(String codeVerifier) {
        if (this.codeChallengeMethod == CodeChallengeMethod.NONE) {
            return this.oAuth2Authentication;
        } else if (this.codeChallengeMethod.transform(codeVerifier).equals(codeChallenge)) {
            return oAuth2Authentication;
        } else {
            throw new InvalidGrantException("Invalid Code verifier");
        }
    }

}
