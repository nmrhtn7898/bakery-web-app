package com.bread.auth.config.custom;

import com.bread.auth.enums.CodeChallengeMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

@RequiredArgsConstructor
public class PkceAuthentication {

    private final String codeChallenge;

    private final CodeChallengeMethod codeChallengeMethod;

    private final OAuth2Authentication oAuth2Authentication;

    public OAuth2Authentication getAuth2Authentication(String codeVerifier) {
        if (codeChallengeMethod.transform(codeVerifier).equals(codeChallenge)) {
            return oAuth2Authentication;
        } else {
            throw new InvalidGrantException("invalid code verifier");
        }
    }

}
