package com.bakery.auth.base;

import com.bakery.auth.config.EmbeddedRedisConfig;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Disabled
@DataRedisTest
@Import(EmbeddedRedisConfig.class)
@ActiveProfiles("test")
public abstract class AbstractDataRedisTest {
}
