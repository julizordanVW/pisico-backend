package com.pisico.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
open class SpringConfig {

    @Bean
    open fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    open fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        // Permitir peticiones desde cualquier origen (ajustar en producción)
        configuration.allowedOrigins = listOf("*")
        // Permitir todos los métodos HTTP (GET, POST, PUT, DELETE, etc.)
        configuration.allowedMethods = listOf("*")
        // Permitir todas las cabeceras
        configuration.allowedHeaders = listOf("*")
        val source = UrlBasedCorsConfigurationSource()
        // Aplicar esta configuración a todas las rutas
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain? {
        return http
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/auth/login/**").permitAll()
                    .requestMatchers("/auth/register/**").permitAll()
                    .requestMatchers("/auth/check-email/**").permitAll()
                    .requestMatchers(
                        "/", "/index.html", "/error", "/webjars/**",
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                        "/properties/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling { e ->
                e
                    .authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .oauth2Login { oauth ->
                oauth
                    .loginPage("/oauth2/authorization/google")
            }
            .csrf { csrf -> csrf.disable() }
            .cors { }
            .build()
    }
}
