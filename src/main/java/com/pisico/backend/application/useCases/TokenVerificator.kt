package com.pisico.backend.application.useCases

import com.pisico.backend.application.ports.out.UserRepository
import com.pisico.backend.domain.entities.UserVerification
import com.pisico.backend.infraestructure.`in`.dto.VerifyEmailRequest
import org.springframework.stereotype.Service

@Service
class TokenVerificator(
    private val userRepository: UserRepository 
) {
    fun verifyEmail(request: VerifyEmailRequest):  UserVerification {
        return userRepository.verifyToken(request.token)
    }
}
