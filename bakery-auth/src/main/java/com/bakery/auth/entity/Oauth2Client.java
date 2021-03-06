package com.bakery.auth.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
@NoArgsConstructor(access = PROTECTED)
@Table(name = "oauth_client_details")
public class Oauth2Client extends BaseEntity {

    private static final long serialVersionUID = 1501999278274720377L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;

    @Column(name = "client_secret", nullable = false)
    private String clientSecret;

    @Column(name = "scope", nullable = false)
    private String scope;

    @Column(name = "resource_ids", nullable = false)
    private String resourceIds;

    @Column(name = "authorized_grant_types", nullable = false)
    private String authorizedGrantTypes;

    @Lob
    @Column(name = "web_server_redirect_uri", length = 65535, nullable = false)
    private String webServerRedirectUri;

    @Column(name = "authorities", nullable = false)
    private String authorities;

    @Column(name = "access_token_validity", nullable = false)
    private Integer accessTokenValidity = 60 * 15;

    @Column(name = "refresh_token_validity", nullable = false)
    private Integer refreshTokenValidity = 60 * 60 * 24 * 5;

    @Column(name = "additional_information")
    private String additionalInformation;

    @Column(name = "auto_approve")
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
