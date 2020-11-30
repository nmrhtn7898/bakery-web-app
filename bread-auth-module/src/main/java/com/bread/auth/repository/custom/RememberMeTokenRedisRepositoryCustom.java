package com.bread.auth.repository.custom;

import java.util.Date;

public interface RememberMeTokenRedisRepositoryCustom {

    void deleteAllByEmail(String email);

    void updateTokenAndLastUsed(String series, String tokenValue, Date lastUsed);

}
