package com.bread.auth.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "oauth_client_details")
public class Oauth2Client extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "client_id", length = 256)
    private String clientId;

    @Column(name = "client_secret", length = 256)
    private String clientSecret;

    @Column(name = "scope", length = 256)
    private String scope;

    @Column(name = "resource_ids", length = 256)
    private String resourceIds;

    @Column(name = "authorized_grant_types", length = 256)
    private String authorizedGrantTypes;

    @Column(name = "web_server_redirect_uri", length = 256)
    private String webServerRedirectUri;

    @Column(name = "authorities", length = 256)
    private String authorities;

    @Column(name = "access_token_validity")
    private Integer accessTokenValidity = 60 * 30;

    @Column(name = "refresh_token_validity")
    private Integer refreshTokenValidity = 60 * 60 * 24 * 7;

    @Column(name = "additional_information", length = 4096)
    private String additionalInformation;

    @Column(name = "autoapprove", length = 256)
    private String autoApprove;

    @Builder
    public Oauth2Client(Long id, String clientId, String clientSecret, String scope, String resourceIds,
                        String authorizedGrantTypes, String webServerRedirectUri, String authorities,
                        Integer accessTokenValidity, Integer refreshTokenValidity, String additionalInformation, String autoApprove) {
        this.id = id;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.resourceIds = resourceIds;
        this.authorizedGrantTypes = authorizedGrantTypes;
        this.webServerRedirectUri = webServerRedirectUri;
        this.authorities = authorities;
        this.accessTokenValidity = accessTokenValidity != null ? accessTokenValidity : this.accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity != null ? refreshTokenValidity : this.refreshTokenValidity;
        this.additionalInformation = additionalInformation;
        this.autoApprove = autoApprove;
    }

}
