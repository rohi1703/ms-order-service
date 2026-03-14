package com.example.orderservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("API documentation for the Order Service, including order creation, retrieval, processing, and filtering.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Order Service Team")
                                .email("support@example.com")
                                .url("https://example.com"))
                );
    }
}