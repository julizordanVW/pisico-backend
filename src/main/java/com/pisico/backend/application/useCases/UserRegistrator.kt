package com.pisico.backend.application.useCases

import com.pisico.backend.application.exception.InvalidUserRegistrationException
import com.pisico.backend.application.ports.out.EmailSenderPort
import com.pisico.backend.application.ports.out.UserRepository
import com.pisico.backend.domain.entities.User
import com.pisico.backend.domain.entities.User.Gender
import com.pisico.backend.infraestructure.`in`.dto.RegisterByEmailRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Service
class UserRegistrator(
    private val userRepository: UserRepository,
    private val emailSender: EmailSenderPort,
    private val passwordEncoder: PasswordEncoder
) {
    fun execute(request: RegisterByEmailRequest) {
        validate(request)

        val hashedPassword = passwordEncoder.encode(request.password)

        val verificationToken = UUID.randomUUID().toString()
        val tokenExpiryDate = OffsetDateTime.now().plusMinutes(20)

        val user = User(
            id = null,
            name = "${request.firstName} ${request.lastName}",
            description = null,
            dateOfBirth = null,
            email = request.email,
            password = hashedPassword,
            emailVerified = false,
            verificationToken = verificationToken,
            tokenExpiryDate = tokenExpiryDate,
            phoneNumber = null,
            profilePictureUrl = null,
            gender = Gender.NOT_SPECIFIED,
            role = null,
            accountStatus = null,
            timeZone = null
        )

        userRepository.save(user, hashedPassword, verificationToken, tokenExpiryDate)
        emailSender.sendVerificationEmail(request.email, verificationToken)
    }

    fun validate(request: RegisterByEmailRequest) {
        if (!User.isValidEmail(request.email)) {
            throw InvalidUserRegistrationException("Invalid email format.")
        }
        if (request.firstName.isEmpty()) {
            throw InvalidUserRegistrationException("First name cannot be empty.")
        }
        if (request.lastName.isEmpty()) {
            throw InvalidUserRegistrationException("Last name cannot be empty.")
        }
        if (!User.isValidPassword(request.password)) {
            throw InvalidUserRegistrationException(
                "Password must be at least 8 characters long, contain uppercase " +
                        "and lowercase letters, a digit, and a special character."
            )
        }
    }
}
