package com.bakery.api.config.custom;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomUserAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public CustomUserAuthenticationToken(TokenUser tokenUser, Object credentials,
                                         Collection<? extends GrantedAuthority> authorities) {
        super(
                tokenUser,
                credentials,
                authorities
        );
    }

}
