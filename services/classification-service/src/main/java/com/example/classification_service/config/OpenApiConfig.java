package com.example.classification_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI classificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Classification Service API")
                        .version("1.0.0")
                        .description("API documentation for Classification Service"));
    }
}