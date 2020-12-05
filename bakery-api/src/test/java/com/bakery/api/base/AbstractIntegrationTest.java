package com.bakery.api.base;

import com.bakery.api.config.EmbeddedRedisConfig;
import com.bakery.api.config.RestDocsConfig;
import com.bakery.api.config.S3MockConfig;
import com.bakery.api.config.custom.WithOauth2AuthenticationTestExecutionListener;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

@Disabled
@SpringBootTest
@ActiveProfiles("test")
@TestExecutionListeners(value = WithOauth2AuthenticationTestExecutionListener.class, mergeMode = MERGE_WITH_DEFAULTS)
@Import(value = { RestDocsConfig.class, S3MockConfig.class, EmbeddedRedisConfig.class })
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

}
