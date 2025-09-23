package com.pisico.backend.application.useCases

import com.pisico.backend.application.exception.InvalidCredentialsException
import com.pisico.backend.application.ports.out.UserRepository
import com.pisico.backend.infraestructure.`in`.dto.LoginByEmailRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserLoginHandler(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun execute(request: LoginByEmailRequest) {
        val user = userRepository.findByEmail(request.email)
            ?: throw InvalidCredentialsException("Invalid email or password.")
        
        if(user.accountStatus != "active") {
            throw InvalidCredentialsException("Account is not active.")
        }
        
        if (user.emailVerified == false) {
            throw InvalidCredentialsException("Email not verified.")
        }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw InvalidCredentialsException("Invalid email or password.")
        }
    }
}
