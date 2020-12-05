package com.bakery.auth.repository;

import com.bakery.auth.model.RememberMeToken;
import com.bakery.auth.repository.custom.RememberMeTokenRedisRepositoryCustom;
import org.springframework.data.repository.CrudRepository;

public interface RememberMeTokenRedisRepository extends CrudRepository<RememberMeToken, String>, RememberMeTokenRedisRepositoryCustom {

    void deleteAllByEmail(String email);

}
