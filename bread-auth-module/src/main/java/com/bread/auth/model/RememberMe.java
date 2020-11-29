package com.bread.auth.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import java.io.Serializable;
import java.util.Date;

import static lombok.AccessLevel.PROTECTED;

@Getter
@RedisHash(value = "rememberMe", timeToLive = 60 * 60 * 24 * 7)
@NoArgsConstructor(access = PROTECTED)
public class RememberMe implements Serializable {

    private static final long serialVersionUID = 270039320612909217L;

    @Id
    private String series;

    @Indexed
    private String email;

    private String token;

    private Date lastUsed;

    public RememberMe(PersistentRememberMeToken token) {
        this.series = token.getSeries();
        this.email = token.getUsername();
        this.token = token.getTokenValue();
        this.lastUsed = token.getDate();
    }

}
