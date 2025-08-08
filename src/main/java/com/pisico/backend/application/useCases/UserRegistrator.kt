package com.pisico.backend.application.useCases

import com.pisico.backend.application.ports.out.UsersRepository
import com.pisico.backend.domain.entities.Gender
import com.pisico.backend.domain.entities.User
import com.pisico.backend.infraestructure.`in`.dto.user.registry.RegisterByEmailRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class UserRegistrator(
    private val usersRepository : UsersRepository,
    private val passwordEncoder: PasswordEncoder

) {
    fun execute(request: RegisterByEmailRequest) {
        val hashedPassword = passwordEncoder.encode(request.password)

        val verificationToken = UUID.randomUUID().toString()
        val tokenExpiryDate = OffsetDateTime.now().plusMinutes(5)

        val user = User(
            id = null,
            name = "${request.firstName} ${request.lastName}",
            description = null,
            dateOfBirth = null,
            email = request.email,
            password = hashedPassword,
            emailVerified = false,
            verification_token = verificationToken,
            token_expiry_date = tokenExpiryDate.toLocalDate(),
            phoneNumber = null,
            profilePictureUrl = null,
            gender = Gender.NOT_SPECIFIED,
            role = null,
            accountStatus = null,
            timeZone = null
        )

        usersRepository.save(user, hashedPassword, verificationToken, tokenExpiryDate)
    }
}
