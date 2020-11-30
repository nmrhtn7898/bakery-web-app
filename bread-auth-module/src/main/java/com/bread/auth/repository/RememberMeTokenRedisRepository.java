package com.bread.auth.repository;

import com.bread.auth.model.RememberMeToken;
import com.bread.auth.repository.custom.RememberMeTokenRedisRepositoryCustom;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RememberMeTokenRedisRepository extends CrudRepository<RememberMeToken, String>, RememberMeTokenRedisRepositoryCustom {

    void deleteAllByEmail(String email);

}
