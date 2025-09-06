package com.pisico.backend.infraestructure.`in`.impl

import com.pisico.backend.application.exception.InvalidCredentialsException
import com.pisico.backend.application.exception.InvalidUserRegistrationException
import com.pisico.backend.application.useCases.UserLoginHandler
import com.pisico.backend.infraestructure.`in`.controller.auth.LoginController
import com.pisico.backend.infraestructure.`in`.dto.LoginByEmailRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class LoginControllerImpl(
    private val userLoginHandler: UserLoginHandler
) : LoginController {
    override fun login(request: LoginByEmailRequest): ResponseEntity<Map<String, Any>> {
        return try {
            userLoginHandler.execute(request)
            val response = mapOf<String, Any>("message" to "Login successful.")
            ResponseEntity.ok(response)
        } catch (e: InvalidCredentialsException) {
            val errorResponse = mapOf<String, Any>("message" to (e.message ?: "Invalid email or password."))
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
        } catch (e: Exception) {
            val errorResponse = mapOf<String, Any>("message" to (e.message ?: "An internal server error occurred."))
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
}