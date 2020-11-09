package com.bread.auth.base;

import com.bread.auth.config.DataConfig;
import com.bread.auth.config.DataConfig.TestProperties;
import com.bread.auth.config.JpaConfig;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.Inheritance;

@Disabled
@Inheritance
@DataJpaTest
@Import(value = {JpaConfig.class, DataConfig.class})
@ActiveProfiles("test")
public abstract class AbstractDataJpaTest {

    @Autowired
    protected TestProperties testProperties;

}
