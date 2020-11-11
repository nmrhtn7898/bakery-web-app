package com.bread.auth.config;

import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

@TestConfiguration
public class RestDocsConfig {

    @Bean
    public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer() {
        CustomUriModifyingOperationPreprocessor uriProcessor = new CustomUriModifyingOperationPreprocessor();
        uriProcessor.setBasePath("/auth"); // /api/v1/example -> /auth/api/v1/example 변환
        uriProcessor.setPort("-1"); // 커스텀 processor 에서 -1 포트는 노출 안함
        uriProcessor.setHost("ec2-54-180-10-196.ap-northeast-2.compute.amazonaws.com");
        return config -> config
                .operationPreprocessors()
                .withRequestDefaults(prettyPrint(), uriProcessor)
                .withResponseDefaults(prettyPrint(), uriProcessor);
    }

    @Bean
    public Jackson2JsonParser jackson2JsonParser() {
        return new Jackson2JsonParser();
    }

}
