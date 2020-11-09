package com.bread.auth.config.custom;

import com.bread.auth.model.AccountAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomJwtTokenConverter extends JwtAccessTokenConverter {

    public CustomJwtTokenConverter(@Value("${jwt.signKey}") String jwtSignKey) {
        setSigningKey(jwtSignKey);
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        String grantType = authentication.getOAuth2Request().getGrantType();
        if (!"client_credentials".equals(grantType)) {
            AccountAdapter account = (AccountAdapter) authentication.getPrincipal();
            Map<String, Object> info = new HashMap<>();
            info.put("user_id", account.getAccount().getId());
            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);
            accessToken = super.enhance(accessToken, authentication);
            accessToken
                    .getAdditionalInformation()
                    .remove("user_id");
            return accessToken;
        } else {
            return super.enhance(accessToken, authentication);
        }
    }

}
