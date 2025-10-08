package com.pisico.backend.infraestructure.`in`.impl

import com.pisico.backend.application.useCases.ValidatePhoneVerificationCode
import com.pisico.backend.domain.exceptions.InvalidVerificationCodeException
import com.pisico.backend.domain.exceptions.MaxAttemptsExceededException
import com.pisico.backend.domain.exceptions.VerificationCodeExpiredException
import com.pisico.backend.infraestructure.`in`.controller.auth.ValidatePhoneVerificationController
import com.pisico.backend.infraestructure.`in`.dto.auth.ValidatePhoneRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ValidatePhoneVerificationControllerImpl(
    private val validatePhoneVerificationCode: ValidatePhoneVerificationCode
) : ValidatePhoneVerificationController {

    override fun validateVerificationCode(request: ValidatePhoneRequest): ResponseEntity<Map<String, Any>> {
        return try {
            validatePhoneVerificationCode.execute(request.phoneNumber, request.code)
            val response = mapOf<String, Any>("message" to "Phone number verified successfully.")
            ResponseEntity.ok(response)
        } catch (e: InvalidVerificationCodeException) {
            val errorResponse = mapOf<String, Any>("message" to (e.message ?: "Invalid verification code."))
            ResponseEntity.badRequest().body(errorResponse)
        } catch (e: MaxAttemptsExceededException) {
            val errorResponse = mapOf<String, Any>("message" to (e.message ?: "Maximum attempts exceeded."))
            ResponseEntity.badRequest().body(errorResponse)
        } catch (e: VerificationCodeExpiredException) {
            val errorResponse = mapOf<String, Any>("message" to (e.message ?: "Verification code expired."))
            ResponseEntity.badRequest().body(errorResponse)
        } catch (_: Exception) {
            val errorResponse = mapOf<String, Any>("message" to "Failed to validate verification code.")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
}
