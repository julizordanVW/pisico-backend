package com.pisico.backend.application.ports.out

import com.pisico.backend.domain.entities.User
import java.time.OffsetDateTime
import java.util.UUID

interface UsersRepository {
    fun verifyEmail(userId: UUID, token: String)
    fun save(user: User, hashedPassword: String, verificationToken: String, tokenExpiryDate: OffsetDateTime)
}