package com.example.moneytracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String bearerAuth = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Money Tracker API")
                        .description("Ledger-based money tracker: dual wallet (Cash & Online), person-wise debt, transfers, reversals. JWT auth.")
                        .version("1.0.0")
                        .contact(new Contact().name("Money Tracker")))
                .addSecurityItem(new SecurityRequirement().addList(bearerAuth))
                .components(new Components()
                        .addSecuritySchemes(bearerAuth,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT from POST /api/auth/login or /api/auth/register")));
    }
}
