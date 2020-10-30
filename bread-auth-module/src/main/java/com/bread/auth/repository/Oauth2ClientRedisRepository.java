package com.bread.auth.repository;

import com.bread.auth.model.Oauth2ClientDetails;
import org.springframework.data.repository.CrudRepository;

public interface Oauth2ClientRedisRepository extends CrudRepository<Oauth2ClientDetails, String> {
}
