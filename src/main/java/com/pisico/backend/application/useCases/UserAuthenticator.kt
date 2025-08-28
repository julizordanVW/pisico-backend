package com.pisico.backend.application.useCases

import com.pisico.backend.application.exception.InvalidUserRegistrationException
import com.pisico.backend.application.ports.out.UsersRepository
import com.pisico.backend.domain.entities.User
import org.springframework.stereotype.Service

@Service
class UserAuthenticator(
    private val usersRepository: UsersRepository,
) {
    fun execute(email: String): Boolean {
        if (!User.isValidEmail(email)) {
            throw InvalidUserRegistrationException("Invalid email format.")
        }

        val user = usersRepository.findByEmail(email)
        return user != null
    }
}
