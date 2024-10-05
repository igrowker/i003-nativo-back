package com.igrowker.nativo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Nativo Banco Rural - Back",
        description ="API Rest para una Fintech. Construida con Java, Spring Boot y PostgreSQL",
        version = "v1.0"
        //contact = @Contact(name= "Igwroker")
        //Licencia?
    ),
    servers = {
        @Server(
                description = "DEV SERVER",
                url = "http://localhost:8080"
        ),
        @Server(
                description = "PROD SERVER",
                url = "https://i003-nativo-back-production.up.railway.app/"
        )
    },
        security = @SecurityRequirement(
                name = "Security Token"
        )
)
@SecuritySchemes(
        @SecurityScheme(
                name = "Security Token",
                description = "Token",
                type = SecuritySchemeType.HTTP,
                in = SecuritySchemeIn.HEADER,
                scheme = "bearer",
                bearerFormat = "JWT"
        )
)
public class SwaggerConfig {}
