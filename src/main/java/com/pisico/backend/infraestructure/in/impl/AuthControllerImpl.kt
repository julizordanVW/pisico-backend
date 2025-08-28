package com.pisico.backend.infraestructure.`in`.impl

import com.pisico.backend.application.useCases.UserAuthenticator
import com.pisico.backend.infraestructure.`in`.controller.auth.AuthController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthControllerImpl(
    private val authenticator: UserAuthenticator,
) : AuthController {
    override fun checkEmailExists(@RequestParam("email") email: String): ResponseEntity<Map<String, Any>> {
        return try {
            val exists = authenticator.execute(email)
            val responseBody = mapOf(
                "exists" to exists,
                "message" to if (exists) "Email already registered." else "Email is available."
            )
            ResponseEntity(responseBody, HttpStatus.OK)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "message" to (e.message ?: "An internal server error occurred")
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
}