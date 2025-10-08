package com.pisico.backend.infraestructure.`in`.impl

import com.pisico.backend.application.exception.EmailAlreadyVerifiedException
import com.pisico.backend.application.exception.InvalidTokenException
import com.pisico.backend.application.useCases.TokenVerificator
import com.pisico.backend.infraestructure.`in`.controller.auth.EmailVerificationController
import com.pisico.backend.infraestructure.`in`.dto.auth.VerifyEmailRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class TokenVerificationImpl(
    private val verificator: TokenVerificator,
) : EmailVerificationController {

    override fun verifyEmail(request: VerifyEmailRequest): ResponseEntity<Map<String, Any>> {
        return try {
            val userVerification = verificator.verifyEmail(request)

            val responseBody = mapOf(
                "success" to true,
                "message" to "Email verified successfully.",
                "user" to mapOf(
                    "email" to userVerification.email,
                    "emailVerified" to userVerification.emailVerified,
                )
            )

            ResponseEntity(responseBody, HttpStatus.OK)
        } catch (e: InvalidTokenException) {
            val errorResponse = mapOf<String, Any>(
                "success" to false,
                "message" to (e.message ?: "Token expired.")
            )
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)

        } catch (e: EmailAlreadyVerifiedException) {
            val errorResponse = mapOf<String, Any>(
                "success" to false,
                "message" to (e.message ?: "Email already verified")
            )
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)

        } catch (e: Exception) {
            val errorResponse = mapOf<String, Any>(
                "success" to false,
                "message" to (e.message ?: "An internal server error occurred")
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }

}