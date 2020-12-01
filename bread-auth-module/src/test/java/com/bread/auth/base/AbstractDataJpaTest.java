package com.bread.auth.base;

import com.bread.auth.config.JpaConfig;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.Inheritance;

@Disabled
@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
public abstract class AbstractDataJpaTest {
}
