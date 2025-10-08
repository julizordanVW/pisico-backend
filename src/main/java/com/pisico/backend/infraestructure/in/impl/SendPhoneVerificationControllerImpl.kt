package com.pisico.backend.infraestructure.`in`.impl

import com.pisico.backend.application.useCases.SendPhoneVerificationCode
import com.pisico.backend.domain.exceptions.RateLimitExceededException
import com.pisico.backend.infraestructure.`in`.controller.auth.SendPhoneVerificationController
import com.pisico.backend.infraestructure.`in`.dto.auth.VerifyPhoneRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class SendPhoneVerificationControllerImpl(
    private val sendPhoneVerificationCode: SendPhoneVerificationCode
) : SendPhoneVerificationController {

    override fun sendVerificationCode(request: VerifyPhoneRequest): ResponseEntity<Map<String, Any>> {
        return try {
            sendPhoneVerificationCode.execute(request)
            val response = mapOf<String, Any>("message" to "Verification code sent successfully.")
            ResponseEntity.ok(response)
        } catch (e: RateLimitExceededException) {
            val errorResponse = mapOf<String, Any>("message" to (e.message ?: "Too many requests."))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse)
        } catch (e: IllegalArgumentException) {
            val errorResponse = mapOf<String, Any>("message" to (e.message ?: "Invalid phone number."))
            ResponseEntity.badRequest().body(errorResponse)
        } catch (_: Exception) {
            val errorResponse = mapOf<String, Any>("message" to "Failed to send verification code.")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
}
