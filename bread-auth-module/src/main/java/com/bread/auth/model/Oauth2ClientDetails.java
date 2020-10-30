package com.bread.auth.model;

import com.bread.auth.entity.Oauth2Client;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import javax.persistence.Id;
import java.io.Serializable;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@RedisHash("client")
public class Oauth2ClientDetails extends BaseClientDetails implements Serializable {

    @Id
    private String id;

    public Oauth2ClientDetails(Oauth2Client client) {
        super(
                client.getClientId(),
                client.getResourceIds(),
                client.getScope(),
                client.getAuthorizedGrantTypes(),
                client.getAuthorities(),
                client.getWebServerRedirectUri()
        );
        this.setClientSecret(client.getClientSecret());
        this.setAccessTokenValiditySeconds(client.getAccessTokenValidity());
        this.setRefreshTokenValiditySeconds(client.getRefreshTokenValidity());
        this.id = client.getClientId();
    }

}
