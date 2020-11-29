package com.bread.auth.repository;

import com.bread.auth.model.RememberMe;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RememberMeRedisRepository extends CrudRepository<RememberMe, String> {

    Optional<RememberMe> findByEmail(String email);

}
