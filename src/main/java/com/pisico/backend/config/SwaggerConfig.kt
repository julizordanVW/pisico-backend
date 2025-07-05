package com.pisico.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "PisiCo API",
        version = "v1",
        description = "API for properties, roommates management and real estate services"
    ),
    servers = [
        Server(url = "http://localhost:8080", description = "Servidor local"),
        Server(url = "https://api.pisico.com", description = "Servidor producción")
    ]
)
open class SwaggerConfig