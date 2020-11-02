package com.bread.auth.base;

import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.Inheritance;

@Disabled
@Inheritance
@WebMvcTest
@ActiveProfiles("test")
public abstract class AbstractWebMvcTest {
}
