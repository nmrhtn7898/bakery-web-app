package com.bread.api.config;

import com.bread.api.config.custom.CustomAccessTokenConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static org.springframework.util.StreamUtils.copyToString;

@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class ResourceConfig extends ResourceServerConfigurerAdapter {

    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter(@Value("${jwt.publicKey.location}") String location, ResourceLoader resourceLoader) throws IOException {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        Resource resource = resourceLoader.getResource(location);
        InputStream inputStream = resource.getInputStream();
        String publicKey = copyToString(inputStream, UTF_8);
        jwtAccessTokenConverter.setVerifierKey(publicKey);
        jwtAccessTokenConverter.setAccessTokenConverter(new CustomAccessTokenConverter());
        return jwtAccessTokenConverter;
    }

    @Bean
    public DefaultTokenServices defaultTokenServices(TokenStore tokenStore, JwtAccessTokenConverter jwtAccessTokenConverter) {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore);
        defaultTokenServices.setTokenEnhancer(jwtAccessTokenConverter);
        return defaultTokenServices;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("api");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                .mvcMatchers("/api/**")
                .and()
                .authorizeRequests()
                .antMatchers(OPTIONS)
                .permitAll()
                .anyRequest()
                .authenticated();
        http
                .sessionManagement()
                .sessionCreationPolicy(STATELESS);
        http
                .csrf()
                .disable();
        http
                .formLogin()
                .disable();
        http
                .logout()
                .disable();
        http
                .exceptionHandling()
                .accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }

}
