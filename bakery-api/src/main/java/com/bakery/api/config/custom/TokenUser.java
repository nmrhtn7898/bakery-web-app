package com.bakery.api.config.custom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

@Getter
@RequiredArgsConstructor
public class TokenUser implements Principal {

    private final Long id;

    private final String name;

}
