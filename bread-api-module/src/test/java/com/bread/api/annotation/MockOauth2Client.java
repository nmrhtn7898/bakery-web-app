package com.bread.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface MockOauth2Client {

    String[] resourceIds() default {"api"};

    String[] scopes() default {"read", "write"};

    String redirectUri() default "/";

    String clientId() default "clientId";

}
