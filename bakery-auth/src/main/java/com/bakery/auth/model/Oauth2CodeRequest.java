package com.bakery.auth.model;

import com.bakery.auth.enums.CodeChallengeMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.io.Serializable;

import static lombok.AccessLevel.PROTECTED;

@Getter
@RedisHash(value = "oauth2CodeRequest", timeToLive = 60 * 10)
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class Oauth2CodeRequest implements Serializable {

    private static final long serialVersionUID = -6511364846319566701L;

    @Id
    private String code;

    private String codeChallenge;

    private CodeChallengeMethod codeChallengeMethod;

    private OAuth2Request oAuth2Request;

    private AccountDetails accountDetails;

    public OAuth2Authentication getAuth2Authentication(String codeVerifier) {
        if (codeChallengeMethod.transform(codeVerifier).equals(codeChallenge)) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    accountDetails,
                    accountDetails.getPassword(),
                    accountDetails.getAuthorities()
            );
            return new OAuth2Authentication(oAuth2Request, authentication);
        } else {
            throw new InvalidGrantException("invalid code verifier");
        }
    }

}
