package com.pisico.backend.application.ports.out

import com.pisico.backend.domain.entities.User
import com.pisico.backend.domain.entities.UserVerification
import com.pisico.backend.jooq.generated.tables.records.UsersRecord
import java.time.OffsetDateTime

interface UserRepository {
    fun findByEmail(email: String): UsersRecord?
    fun verifyToken(token: String): UserVerification
    fun save(user: User, hashedPassword: String, verificationToken: String, tokenExpiryDate: OffsetDateTime?)
    fun updateVerificationToken(email: String, token: String, expiryDate: OffsetDateTime)
    fun updateUserByEmail(email: String)
}