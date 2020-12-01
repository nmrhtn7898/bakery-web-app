package com.bread.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface MockOauth2User {

    long userId() default 1L;

    String username() default "user";

    String credentials() default "N/A";

    String[] authorities() default {"user", "admin"};

}
