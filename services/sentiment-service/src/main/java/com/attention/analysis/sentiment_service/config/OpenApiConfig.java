package com.attention.analysis.sentiment_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI sentimentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sentiment Service API")
                        .version("1.0.0")
                        .description("API documentation for Sentiment Service"));
    }
}