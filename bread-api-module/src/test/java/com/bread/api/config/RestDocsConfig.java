package com.bread.api.config;

import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.operation.preprocess.UriModifyingOperationPreprocessor;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

@TestConfiguration
public class RestDocsConfig {

    @Bean
    public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer() {
        UriModifyingOperationPreprocessor preprocessor = new UriModifyingOperationPreprocessor();
        preprocessor
                .scheme("https")
                .host("dev.bread-project.tk")
                .removePort();
        return config -> config
                .operationPreprocessors()
                .withRequestDefaults(prettyPrint(), preprocessor)
                .withResponseDefaults(prettyPrint(), preprocessor);
    }

    @Bean
    public Jackson2JsonParser jackson2JsonParser() {
        return new Jackson2JsonParser();
    }

}
