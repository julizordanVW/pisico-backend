package com.pisico.backend.infraestructure.`in`.impl

import com.pisico.backend.application.exception.InvalidUserRegistrationException
import com.pisico.backend.application.useCases.UserEmailChecker
import com.pisico.backend.infraestructure.`in`.controller.auth.CheckEmailController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CheckEmailControllerImpl(
    private val authenticator: UserEmailChecker,
) : CheckEmailController {
    override fun checkEmailExists(@RequestParam("email") email: String): ResponseEntity<Map<String, Any>> {
        return try {
            val exists = authenticator.execute(email)
            val responseBody = mapOf(
                "exists" to exists,
                "message" to if (exists) "Email already registered." else "Email is available."
            )
            ResponseEntity(responseBody, HttpStatus.OK)
        } catch (e: InvalidUserRegistrationException) {
            val errorResponse = mapOf<String, Any>("message" to (e.message ?: "Invalid email format."))
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "message" to (e.message ?: "An internal server error occurred")
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
}