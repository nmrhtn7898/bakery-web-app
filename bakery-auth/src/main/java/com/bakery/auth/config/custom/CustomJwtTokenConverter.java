package com.bakery.auth.config.custom;

import com.bakery.auth.model.AccountDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

import static com.bakery.auth.enums.Oauth2GrantType.CLIENT_CREDENTIALS;

@Component
public class CustomJwtTokenConverter extends JwtAccessTokenConverter {

    public CustomJwtTokenConverter(@Value("${jwt.keyPair.location}") String location, @Value("${jwt.keyPair.storePass}") String storePass,
                                   @Value("${jwt.keyPair.alias}") String alias, ResourceLoader resourceLoader) {
        Resource resource = resourceLoader.getResource(location);
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, storePass.toCharArray());
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias);
        setKeyPair(keyPair);
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        String grantType = authentication.getOAuth2Request().getGrantType();
        if (!CLIENT_CREDENTIALS.toString().equals(grantType)) {
            AccountDetails account = (AccountDetails) authentication.getPrincipal();
            Map<String, Object> info = new HashMap<>();
            info.put("user_id", account.getId());
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
