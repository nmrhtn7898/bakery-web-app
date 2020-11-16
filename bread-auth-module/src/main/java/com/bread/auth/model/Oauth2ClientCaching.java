package com.bread.auth.model;

import com.bread.auth.entity.Oauth2Client;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import java.io.Serializable;

import static java.util.Arrays.asList;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@RedisHash(value = "client", timeToLive = 60)
public class Oauth2ClientCaching extends BaseClientDetails implements Serializable {

    @Id
    private String id;

    public Oauth2ClientCaching(Oauth2Client client) {
        super(
                client.getClientId(),
                client.getResourceIds(),
                client.getScope(),
                client.getAuthorizedGrantTypes(),
                client.getAuthorities(),
                client.getWebServerRedirectUri()
        );
        setClientSecret(client.getClientSecret());
        setAccessTokenValiditySeconds(client.getAccessTokenValidity());
        setRefreshTokenValiditySeconds(client.getRefreshTokenValidity());
        if (client.getAutoApprove() != null) {
            setAutoApproveScopes(asList(client.getAutoApprove().split(",")));
        }
        id = client.getClientId();
    }

}
