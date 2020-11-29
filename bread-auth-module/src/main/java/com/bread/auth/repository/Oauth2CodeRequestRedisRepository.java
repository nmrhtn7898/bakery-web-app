package com.bread.auth.repository;

import com.bread.auth.model.Oauth2CodeRequest;
import org.springframework.data.repository.CrudRepository;

public interface Oauth2CodeRequestRedisRepository extends CrudRepository<Oauth2CodeRequest, String> {
}
