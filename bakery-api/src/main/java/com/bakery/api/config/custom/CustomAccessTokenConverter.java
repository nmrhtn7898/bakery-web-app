package com.bakery.api.config.custom;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.lang.Long.parseLong;

@Component
public class CustomAccessTokenConverter extends DefaultAccessTokenConverter {

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        OAuth2Authentication oAuth2Authentication = super.extractAuthentication(map);
        Authentication userAuthentication = oAuth2Authentication.getUserAuthentication();
        Long id = parseLong(map.get("user_id").toString());
        TokenUser tokenUser = new TokenUser(id, userAuthentication.getName());
        CustomUserAuthenticationToken customUserAuthenticationToken = new CustomUserAuthenticationToken(
                tokenUser,
                userAuthentication.getCredentials(),
                userAuthentication.getAuthorities()
        );
        return new OAuth2Authentication(
                oAuth2Authentication.getOAuth2Request(),
                customUserAuthenticationToken
        );
    }

}
