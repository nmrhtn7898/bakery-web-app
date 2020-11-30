package com.bread.auth.config.custom;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.util.StringUtils.hasText;

public class CustomRememberMeService extends PersistentTokenBasedRememberMeServices {

    private final CustomRememberMeTokenRepository persistentTokenRepository;

    public CustomRememberMeService(String key, UserDetailsService userDetailsService, CustomRememberMeTokenRepository tokenRepository) {
        super(key, userDetailsService, tokenRepository);
        this.persistentTokenRepository = tokenRepository;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            String rememberMeCookie = extractRememberMeCookie(request);
            if (hasText(rememberMeCookie)) {
                String series = decodeCookie(rememberMeCookie)[0];
                persistentTokenRepository.removeUserToken(series);
            }
        }
        logger.debug("Logout of user " + (authentication == null ? "Unknown" : authentication.getName()));
        cancelCookie(request, response);
    }

}
