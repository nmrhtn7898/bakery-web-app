package com.bread.api.model;

import lombok.Getter;

@Getter
public class LoginToken {

    private String access_token;

    private String token_type;

    private String refresh_token;

    private Integer expires_in;

    private String scope;

    private String jti;

}
