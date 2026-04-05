package com.easydrugs.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration Swagger / OpenAPI 3.
 * Accessible via : http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("EASY-DRUGS API")
                .version("1.0.0")
                .description("""
                    API REST pour la géolocalisation de médicaments
                    dans les pharmacies de **Sangmélima**, Cameroun.

                    **Prototype** : 2 pharmacies pilotes — zone géographique restreinte.

                    ### Authentification
                    Toutes les routes protégées nécessitent un token **JWT Bearer**.
                    Obtenez-le via `POST /api/auth/login`.
                    """)
                .contact(new Contact()
                    .name("EASY-DRUGS Team")
                    .email("contact@easy-drugs.cm"))
                .license(new License()
                    .name("Propriétaire — EASY-DRUGS 2026"))
            )
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Développement local"),
                new Server().url("https://api.easy-drugs.app").description("Production")
            ))
            // Schéma de sécurité JWT
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Entrez votre token JWT (sans le préfixe 'Bearer ')")
                )
            );
    }
}
