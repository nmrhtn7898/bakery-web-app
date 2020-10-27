package com.bread.auth.model;

import com.bread.auth.entity.Oauth2Client;
import lombok.Getter;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

@Getter
public class ClientAdapter extends BaseClientDetails {

    private final Oauth2Client client;

    public ClientAdapter(Oauth2Client client) {
        super(
                client.getClientId(),
                client.getResourceIds(),
                client.getScope(),
                client.getAuthorizedGrantTypes(),
                client.getAuthorities(),
                client.getWebServerRedirectUri()
        );
        super.setClientSecret(client.getClientSecret());
        super.setAccessTokenValiditySeconds(client.getAccessTokenValidity());
        super.setRefreshTokenValiditySeconds(client.getRefreshTokenValidity());
        this.client = client;
    }

}
