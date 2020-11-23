package com.bread.common;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@Inherited
@Import(StringEncryptorConfig.class)
public @interface EnableEncryptProperty {
}
