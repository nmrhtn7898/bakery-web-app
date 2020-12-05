package com.bakery.auth.enums;

public enum Oauth2GrantType {

    AUTHORIZATION_CODE,
    IMPLICIT,
    PASSWORD,
    REFRESH_TOKEN,
    CLIENT_CREDENTIALS;


    @Override
    public String toString() {
        return super
                .toString()
                .toLowerCase();
    }

}
