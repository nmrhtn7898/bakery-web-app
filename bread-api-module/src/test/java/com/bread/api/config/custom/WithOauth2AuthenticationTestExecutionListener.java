package com.bread.api.config.custom;

import com.bread.api.annotation.MockOauth2Authentication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WithOauth2AuthenticationTestExecutionListener extends DependencyInjectionTestExecutionListener {

    /**
     * API 테스트시, 토큰 발급을 위한 인증 서버와의 테스트 의존성을 제거하기 위해 토큰 인증 처리 과정 Mocking
     *
     * @param testContext 테스트 컨텍스트
     */
    @Override
    public void beforeTestMethod(TestContext testContext) {
        ApplicationContext applicationContext = testContext.getApplicationContext();
        DefaultTokenServices defaultTokenServices = applicationContext.getBean(DefaultTokenServices.class);
        TokenStore tokenStore = mock(TokenStore.class);
        OAuth2AccessToken oAuth2AccessToken = mock(OAuth2AccessToken.class);
        Method testMethod = testContext.getTestMethod();
        if (testMethod.isAnnotationPresent(MockOauth2Authentication.class)) {
            MockOauth2Authentication annotation = testMethod.getAnnotation(MockOauth2Authentication.class);
            Map<String, String> params = new HashMap<>();
            params.put("client_id", annotation.client().clientId());
            List<SimpleGrantedAuthority> authorities = stream(annotation.user().authorities())
                    .map(SimpleGrantedAuthority::new)
                    .collect(toList());
            OAuth2Request oauth2Request = new OAuth2Request(
                    params,
                    annotation.client().clientId(),
                    authorities,
                    true,
                    new HashSet<>(asList(annotation.client().scopes())),
                    new HashSet<>(asList(annotation.client().resourceIds())),
                    annotation.client().redirectUri(),
                    null,
                    null
            );
            CustomUserAuthenticationToken customUserAuthenticationToken = new CustomUserAuthenticationToken(
                    new TokenUser(annotation.user().userId(), annotation.user().username()),
                    annotation.user().credentials(),
                    authorities
            );
            OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oauth2Request, customUserAuthenticationToken);
            when(tokenStore.readAccessToken("{access_token}")).thenReturn(oAuth2AccessToken);
            when(oAuth2AccessToken.isExpired()).thenReturn(false);
            when(tokenStore.readAuthentication(oAuth2AccessToken)).thenReturn(oAuth2Authentication);
            defaultTokenServices.setTokenStore(tokenStore);
        }
    }

}
