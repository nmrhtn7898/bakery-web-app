package com.bread.auth.repository;

import com.bread.auth.model.RememberMeCaching;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RememberMeRedisRepository extends CrudRepository<RememberMeCaching, String> {

    Optional<RememberMeCaching> findByEmail(String email);

}
