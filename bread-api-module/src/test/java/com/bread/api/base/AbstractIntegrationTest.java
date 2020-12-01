package com.bread.api.base;

import com.bread.api.config.custom.WithOauth2AuthenticationTestExecutionListener;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

@Disabled
@SpringBootTest
@ActiveProfiles("test")
@TestExecutionListeners(value = WithOauth2AuthenticationTestExecutionListener.class, mergeMode = MERGE_WITH_DEFAULTS)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

}
