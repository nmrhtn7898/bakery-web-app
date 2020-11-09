package com.bread.auth.repository.redis;

import com.bread.auth.model.Oauth2ClientCaching;
import org.springframework.data.repository.CrudRepository;

public interface Oauth2ClientRedisRepository extends CrudRepository<Oauth2ClientCaching, String> {
}
