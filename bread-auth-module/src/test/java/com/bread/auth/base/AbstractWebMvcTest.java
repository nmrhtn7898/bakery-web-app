package com.bread.auth.base;

import com.bread.auth.config.AuthConfig;
import com.bread.auth.config.RestDocsConfig;
import com.bread.auth.config.SecurityConfig;
import com.bread.auth.config.TestDataConfig.TestProperties;
import com.bread.auth.config.custom.CustomJwtTokenConverter;
import com.bread.auth.config.custom.CustomRememberMeTokenRepository;
import com.bread.auth.config.custom.PkceAuthorizationCodeService;
import com.bread.auth.entity.Account;
import com.bread.auth.entity.AccountAuthority;
import com.bread.auth.entity.Authority;
import com.bread.auth.entity.Oauth2Client;
import com.bread.auth.model.AccountDetails;
import com.bread.auth.model.Oauth2ClientDetails;
import com.bread.auth.service.Oauth2ClientService;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

@Disabled
@WebMvcTest
@ActiveProfiles("test")
@Import(value = {RestDocsConfig.class, SecurityConfig.class, AuthConfig.class, TestProperties.class})
@AutoConfigureRestDocs
public abstract class AbstractWebMvcTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected TestProperties testProperties;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private CustomRememberMeTokenRepository customRememberMeTokenRepository;

    @MockBean
    private Oauth2ClientService clientDetailsService;

    @MockBean
    private CustomJwtTokenConverter customJwtTokenConverter;

    @MockBean
    private PkceAuthorizationCodeService pkceAuthorizationCodeService;

    @MockBean
    private TokenStore tokenStore;

    @PostConstruct
    public void postConstruct() {
        OAuth2Authentication oAuth2Authentication = generateOauth2Authentication();
        DefaultOAuth2AccessToken oAuth2AccessToken = generateAccessToken();
        when(userDetailsService.loadUserByUsername(any())).thenReturn(generateAccountDetails());
        when(clientDetailsService.loadClientByClientId(any())).thenReturn(generateOauth2ClientDetails());
        when(customJwtTokenConverter.enhance(any(), any())).thenReturn(generateAccessToken());
        doReturn(generateCheckTokenInfo()).when(customJwtTokenConverter).convertAccessToken(any(), any());
        doAnswer(invocation -> null).when(pkceAuthorizationCodeService).removeStoredAuthentication(any());
        when(pkceAuthorizationCodeService.consumeAuthorizationCodeAndCodeVerifier(any(), any())).thenReturn(oAuth2Authentication);
        when(pkceAuthorizationCodeService.createAuthorizationCode(any())).thenReturn("JXrJ2y");
        when(tokenStore.readRefreshToken(any())).thenReturn(generateRefreshToken());
        when(tokenStore.readAccessToken(any())).thenReturn(oAuth2AccessToken);
        when(tokenStore.readAuthentication(any(OAuth2AccessToken.class))).thenReturn(oAuth2Authentication);
        when(tokenStore.readAuthenticationForRefreshToken(any())).thenReturn(oAuth2Authentication);
    }

    private AccountDetails generateAccountDetails() {
        Account account = Account
                .builder()
                .email(testProperties.getUsers().getMaster().getUsername())
                .password("{noop}" + testProperties.getUsers().getMaster().getPassword())
                .build();
        testProperties
                .getUsers()
                .getMaster()
                .getAuthorities()
                .forEach(authority ->
                        AccountAuthority
                                .builder()
                                .account(account)
                                .authority(
                                        Authority
                                                .builder()
                                                .name(authority)
                                                .build()
                                )
                                .build()
                );
        return new AccountDetails(account);
    }

    private Oauth2ClientDetails generateOauth2ClientDetails() {
        Oauth2Client oauth2Client = Oauth2Client
                .builder()
                .clientId(testProperties.getClients().getMaster().getClientId())
                .clientSecret("{noop}" + testProperties.getClients().getMaster().getClientSecret())
                .scope(testProperties.getClients().getMaster().getScopes())
                .resourceIds(testProperties.getClients().getMaster().getResourceIds())
                .authorities(testProperties.getClients().getMaster().getAuthorities())
                .webServerRedirectUri(testProperties.getClients().getMaster().getRedirectUris())
                .authorizedGrantTypes(testProperties.getClients().getMaster().getGrantTypes())
                .build();
        return new Oauth2ClientDetails(oauth2Client);
    }

    private DefaultOAuth2AccessToken generateAccessToken() {
        DefaultOAuth2AccessToken defaultOAuth2AccessToken = new DefaultOAuth2AccessToken("{access_token}");
        defaultOAuth2AccessToken.setExpiration(new Date(currentTimeMillis() + (60 * 15 * 1000L)));
        defaultOAuth2AccessToken.setTokenType("bearer");
        DefaultExpiringOAuth2RefreshToken oAuth2RefreshToken = generateRefreshToken();
        defaultOAuth2AccessToken.setRefreshToken(oAuth2RefreshToken);
        defaultOAuth2AccessToken.setScope(
                new HashSet<>(
                        asList(
                                testProperties
                                        .getClients()
                                        .getMaster()
                                        .getScopes()
                                        .split(",")
                        )
                )
        );
        defaultOAuth2AccessToken.setAdditionalInformation(singletonMap("jti", randomUUID().toString()));
        return defaultOAuth2AccessToken;
    }

    private DefaultExpiringOAuth2RefreshToken generateRefreshToken() {
        return new DefaultExpiringOAuth2RefreshToken(
                "{refresh_token}",
                new Date(currentTimeMillis() + (60 * 60 * 24 * 5 * 1000L))
        );
    }

    private OAuth2Authentication generateOauth2Authentication() {
        OAuth2Request oAuth2Request = new OAuth2Request(
                singletonMap("client_id", testProperties.getClients().getMaster().getClientId()),
                testProperties.getClients().getMaster().getClientId(),
                createAuthorityList(
                        testProperties
                                .getClients()
                                .getMaster()
                                .getAuthorities()
                                .split(",")
                ),
                true,
                new HashSet<>(
                        asList(
                                testProperties
                                        .getClients()
                                        .getMaster()
                                        .getScopes()
                                        .split(",")
                        )
                ),
                new HashSet<>(
                        asList(
                                testProperties
                                        .getClients()
                                        .getMaster()
                                        .getRedirectUris()
                                        .split(",")
                        )
                ),
                testProperties.getClients().getMaster().getRedirectUris().split(",")[0],
                new HashSet<>(),
                new HashMap<>()
        );
        return new OAuth2Authentication(
                oAuth2Request,
                generateUserAuthentication()
        );

    }

    private UsernamePasswordAuthenticationToken generateUserAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                testProperties.getUsers().getMaster().getUsername(),
                "N/A",
                createAuthorityList(
                        testProperties
                                .getClients()
                                .getMaster()
                                .getAuthorities()
                                .split(",")
                )
        );
    }

    private Map<String, Object> generateCheckTokenInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("aud", testProperties.getClients().getMaster().getResourceIds().split(","));
        map.put("user_id", 17123L);
        map.put("user_name", testProperties.getUsers().getMaster().getUsername());
        map.put("scope", testProperties.getClients().getMaster().getScopes().split(","));
        map.put("active", true);
        map.put("exp", currentTimeMillis() / 1000 + (60 * 15));
        map.put("authorities", testProperties.getClients().getMaster().getAuthorities().split(","));
        map.put("jti", randomUUID().toString());
        map.put("client_id", testProperties.getClients().getMaster().getClientId());
        return map;
    }


}
