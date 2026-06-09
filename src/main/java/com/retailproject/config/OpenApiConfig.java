package com.retailproject.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI retailProjectOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RetailProject API")
                        .description("Spring Boot API for the retail analytics dashboard")
                        .version("1.0")
                        .contact(new Contact().name("RetailProject")));
    }
}
