package com.pisico.backend.application.useCases

import com.pisico.backend.application.exception.EmailAlreadyVerifiedException
import com.pisico.backend.application.exception.InvalidCredentialsException
import com.pisico.backend.application.exception.TooManyRequestsException
import com.pisico.backend.application.ports.out.EmailSenderPort
import com.pisico.backend.application.ports.out.UserRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Service
class VerificationEmailResender(
    private val userRepository: UserRepository,
    private val emailSender: EmailSenderPort,
) {
    fun execute(email: String) {
        val user = userRepository.findByEmail(email)
            ?: throw InvalidCredentialsException("User with email $email not found")

        if (user.emailVerified) {
            throw EmailAlreadyVerifiedException("Email already verified")
        }

        val lastUpdate = user.rowUpdatedOn
        if (lastUpdate != null) {
            val minutesSinceLastEmail = Duration.between(lastUpdate, LocalDateTime.now()).toMinutes()
            if (minutesSinceLastEmail < 2) {
                val remainingMinutes = 2 - minutesSinceLastEmail
                throw TooManyRequestsException("Please wait $remainingMinutes minute(s) before resending")
            }
        }
        
        val verificationToken = UUID.randomUUID().toString()
        val tokenExpiryDate = OffsetDateTime.now().plusMinutes(20)
        
        userRepository.updateVerificationToken(email, verificationToken, tokenExpiryDate)
        emailSender.sendVerificationEmail(email, verificationToken)
    }
}
