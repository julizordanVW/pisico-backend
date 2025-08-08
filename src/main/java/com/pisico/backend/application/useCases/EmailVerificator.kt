package com.pisico.backend.application.useCases

import com.pisico.backend.infraestructure.out.UserAdapter
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EmailVerificator(
    private val userAdapter : UserAdapter
) {
    fun verifyEmail(userId: String, token: String) {
        userAdapter.verifyEmail(UUID.fromString(userId), token)
    }
}
