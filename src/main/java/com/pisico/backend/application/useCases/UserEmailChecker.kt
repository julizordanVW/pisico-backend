package com.pisico.backend.application.useCases

import com.pisico.backend.application.exception.InvalidUserRegistrationException
import com.pisico.backend.application.ports.out.UserRepository
import com.pisico.backend.domain.entities.User
import org.springframework.stereotype.Service

@Service
class UserEmailChecker(
    private val userRepository: UserRepository,
) {
    fun execute(email: String): Boolean {
        if (!User.isValidEmail(email.trim())) {
            throw InvalidUserRegistrationException("Invalid email format.")
        }
        return userRepository.findByEmail(email) != null
    }
}
