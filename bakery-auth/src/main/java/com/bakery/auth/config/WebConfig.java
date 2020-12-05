package com.bakery.auth.config;

import com.bakery.common.EnableEncryptProperty;
import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableEncryptProperty
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> factory.addContextCustomizers(context ->
                context.setCookieProcessor(new LegacyCookieProcessor())
        );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/")
                .allowCredentials(true)
                .allowedOrigins("*")
                .allowedMethods("*");
    }

}
