package com.bread.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface MockOauth2Authentication {

    MockOauth2Client client() default @MockOauth2Client();

    MockOauth2User user() default @MockOauth2User();

}
