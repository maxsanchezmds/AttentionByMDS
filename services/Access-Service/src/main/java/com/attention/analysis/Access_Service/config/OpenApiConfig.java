package com.attention.analysis.Access_Service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI accessServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Access Service API")
                        .version("1.0.0")
                        .description("API documentation for Access Service"));
    }
}