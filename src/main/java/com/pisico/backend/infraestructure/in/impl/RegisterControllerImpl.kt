package com.pisico.backend.infraestructure.`in`.impl

import com.pisico.backend.application.exception.InvalidUserRegistrationException
import com.pisico.backend.application.useCases.UserRegistrator
import com.pisico.backend.infraestructure.`in`.controller.auth.RegisterController
import com.pisico.backend.infraestructure.`in`.dto.RegisterByEmailRequest
import com.pisico.backend.infraestructure.mapper.UserMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class RegisterControllerImpl(
    private val userRegistrator: UserRegistrator,
) : RegisterController {

    override fun register(registerByEmailRequest: RegisterByEmailRequest): ResponseEntity<Map<String, Any>> {
        return try {
            userRegistrator.execute(registerByEmailRequest)
            val response = mapOf<String, Any>("message" to "User registered successfully")
            ResponseEntity.ok(response)
        } catch (e: InvalidUserRegistrationException) {
            val errorResponse = mapOf<String, Any>("message" to (e.message ?: "Invalid user registration data"))
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
        } catch (e: Exception) {
            val errorResponse = mapOf<String, Any>("message" to (e.message ?: "An internal server error occurred"))
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
}