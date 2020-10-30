package com.bread.auth.base;

import com.bread.auth.config.EmbeddedRedisConfig;
import com.bread.auth.config.RestDocsConfig;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.Inheritance;

@Disabled
@SpringBootTest
@Inheritance
@ActiveProfiles("test")
@Import(value = {RestDocsConfig.class, EmbeddedRedisConfig.class})
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected Jackson2JsonParser parser;

}
