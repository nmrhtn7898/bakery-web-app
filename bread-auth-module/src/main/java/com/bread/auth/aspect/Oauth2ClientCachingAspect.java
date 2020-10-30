package com.bread.auth.aspect;

import com.bread.auth.model.Oauth2ClientDetails;
import com.bread.auth.repository.Oauth2ClientRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class Oauth2ClientCachingAspect {

    private final Oauth2ClientRedisRepository oauth2ClientRedisRepository;

    @Around("execution(* com.bread.auth.service.Oauth2ClientService.loadClientByClientId(String))")
    public Object caching(ProceedingJoinPoint joinPoint) throws Throwable {
        String clientId = (String) joinPoint.getArgs()[0];
        Optional<Oauth2ClientDetails> byId = oauth2ClientRedisRepository.findById(clientId);
        if (byId.isPresent()) {
            log.info("clientId : {} is cached", clientId);
            return byId.get();
        } else {
            log.info("caching clientId : {}", clientId);
            Oauth2ClientDetails oauth2ClientDetails = (Oauth2ClientDetails) joinPoint.proceed();
            oauth2ClientDetails = oauth2ClientRedisRepository.save(oauth2ClientDetails);
            return oauth2ClientDetails;
        }
    }

}
