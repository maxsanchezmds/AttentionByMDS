package com.attention.analysis.Message_Receptionist.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI messageReceptionistOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Message Receptionist API")
                        .version("1.0.0")
                        .description("API documentation for Message Receptionist"));
    }
}