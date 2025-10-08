package com.pisico.backend.infraestructure.`in`.impl

import com.pisico.backend.application.exception.EmailAlreadyVerifiedException
import com.pisico.backend.application.exception.InvalidCredentialsException
import com.pisico.backend.application.exception.TooManyRequestsException
import com.pisico.backend.application.useCases.VerificationEmailResender
import com.pisico.backend.infraestructure.`in`.controller.auth.ResendEmailController
import com.pisico.backend.infraestructure.`in`.dto.auth.ResendEmailRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ResendEmailControllerImpl(
    private val verificationEmailResender: VerificationEmailResender,
) : ResendEmailController {

    override fun resendEmail(request: ResendEmailRequest): ResponseEntity<Map<String, Any>> {

        return try {
            verificationEmailResender.execute(request.email)
            ResponseEntity.ok(mapOf("message" to "Verification email sent successfully"))

        } catch (_: InvalidCredentialsException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("message" to "User not found"))

        } catch (_: TooManyRequestsException) {
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(mapOf("message" to "Please wait before resending"))

        } catch (_: EmailAlreadyVerifiedException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Email already verified"))

        } catch (_: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "An internal server error occurred"))
        }
    }
}