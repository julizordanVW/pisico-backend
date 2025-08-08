package com.pisico.backend.infraestructure.`in`.impl

import com.pisico.backend.application.useCases.EmailVerificator
import com.pisico.backend.application.useCases.UserRetriever
import com.pisico.backend.infraestructure.`in`.controller.auth.EmailVerificationController
import org.springframework.web.bind.annotation.RestController

@RestController
class EmailVerificationImpl(
    private val verificator: EmailVerificator,
) : EmailVerificationController {
    override fun verifyEmail(userId: String, token: String) {
        verificator.verifyEmail(userId, token)
    }

}