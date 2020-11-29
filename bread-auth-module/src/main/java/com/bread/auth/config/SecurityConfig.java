package com.bread.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;

    private final PersistentTokenRepository persistentTokenRepository;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers()
                .frameOptions()
                .sameOrigin();
        http
                .formLogin()
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    String redirectUri = request.getParameter("redirect_uri");
                    response.sendRedirect(redirectUri);
                });
        http
                .logout()
                .logoutSuccessUrl("/login");
        http
                .rememberMe()
                .tokenValiditySeconds(60 * 60 * 24 * 7)
                .key("rememberMeServices")
                .tokenRepository(persistentTokenRepository)
                .rememberMeParameter("remember-me")
                .rememberMeCookieName("remember-me")
//                .useSecureCookie(true)
                .userDetailsService(userDetailsService);
        http
                .requestMatchers()
                .mvcMatchers("/login", "/logout", "/oauth/authorize")
                .and()
                .authorizeRequests()
                .mvcMatchers("/login").permitAll()
                .anyRequest()
                .authenticated();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .mvcMatchers("/docs/**");
    }

}
