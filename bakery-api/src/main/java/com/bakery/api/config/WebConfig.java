package com.bakery.api.config;

import com.bakery.common.EnableEncryptProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableEncryptProperty
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/")
                .allowCredentials(true)
                .allowedOrigins("*")
                .allowedMethods("*");
    }

}
