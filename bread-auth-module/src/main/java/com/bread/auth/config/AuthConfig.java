package com.bread.auth.config;

import com.bread.auth.service.Oauth2ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
@RequiredArgsConstructor
public class AuthConfig extends AuthorizationServerConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final TokenStore tokenStore;

    private final AccessTokenConverter accessTokenConverter;

    private final UserDetailsService userDetailsService;

    private final Oauth2ClientService clientDetailsService;

    private final PkceAuthorizationCodeService pkceAuthorizationCodeService;


    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security
                .checkTokenAccess("isAuthenticated()")
                .passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
                .withClientDetails(clientDetailsService);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        PkceAuthorizationCodeTokenGranter tokenGranter = new PkceAuthorizationCodeTokenGranter(
                endpoints.getTokenServices(),
                pkceAuthorizationCodeService,
                clientDetailsService,
                endpoints.getOAuth2RequestFactory()
        );
        CustomCompositeTokenGranter compositeTokenGranter = new CustomCompositeTokenGranter(
                clientDetailsService,
                endpoints.getAuthorizationCodeServices(),
                endpoints.getTokenServices(),
                endpoints.getOAuth2RequestFactory(),
                authenticationManager
        );
        compositeTokenGranter.addGranter(tokenGranter);
        endpoints
                .authorizationCodeServices(pkceAuthorizationCodeService)
                .tokenGranter(compositeTokenGranter)
                .userDetailsService(userDetailsService)
                .authenticationManager(authenticationManager)
                .accessTokenConverter(accessTokenConverter)
                .tokenStore(tokenStore);
    }

}
