package com.pisico.backend.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val requestURI = request.requestURI

        // Si la ruta empieza con /auth/ pero no existe, devolver 404
        if (requestURI.startsWith("/auth/") && !isValidAuthEndpoint(requestURI)) {
            response.status = HttpStatus.NOT_FOUND.value()
            response.contentType = "application/json"
            response.writer.write("""{"error": "Endpoint not found", "status": 404}""")
        } else {
            // Para otras rutas protegidas, devolver 401
            response.status = HttpStatus.UNAUTHORIZED.value()
            response.contentType = "application/json"
            response.writer.write("""{"error": "Unauthorized", "status": 401}""")
        }
    }

    private fun isValidAuthEndpoint(uri: String): Boolean {
        val validEndpoints = listOf(
            "/auth/login",
            "/auth/register",
            "/auth/check-email",
            "/auth/verify"
        )
        return validEndpoints.any { endpoint ->
            uri == endpoint || uri.startsWith("$endpoint/")
        }
    }
}