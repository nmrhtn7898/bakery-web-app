package com.bread.auth.config;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenGranter;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.implicit.ImplicitTokenGranter;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.ArrayList;
import java.util.List;

public class CustomCompositeTokenGranter implements TokenGranter {

    private final CompositeTokenGranter compositeTokenGranter;

    public CustomCompositeTokenGranter(ClientDetailsService cds, AuthorizationCodeServices acs,
                                       AuthorizationServerTokenServices asts, OAuth2RequestFactory orf,
                                       AuthenticationManager am) {
        List<TokenGranter> tokenGranters = new ArrayList<>();
//        tokenGranters.add(new AuthorizationCodeTokenGranter(asts, acs, cds, orf)); PkceAuthorizationCodeTokenGranter 에서 구현
        tokenGranters.add(new RefreshTokenGranter(asts, cds, orf));
        tokenGranters.add(new ImplicitTokenGranter(asts, cds, orf));
        tokenGranters.add(new ClientCredentialsTokenGranter(asts, cds, orf));
        if (am != null) {
            tokenGranters.add(new ResourceOwnerPasswordTokenGranter(am, asts, cds, orf));
        }
        this.compositeTokenGranter = new CompositeTokenGranter(tokenGranters);
    }

    public void addGranter(TokenGranter tokenGranter) {
        this.compositeTokenGranter.addTokenGranter(tokenGranter);
    }

    @Override
    public OAuth2AccessToken grant(String grantType, TokenRequest tokenRequest) {
        return compositeTokenGranter.grant(grantType, tokenRequest);
    }

}
