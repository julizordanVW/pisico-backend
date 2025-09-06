package com.pisico.backend.application.ports.out

import com.pisico.backend.domain.entities.User
import com.pisico.backend.jooq.generated.tables.records.UsersRecord
import java.time.OffsetDateTime
import java.util.UUID

interface UserRepository {
    fun findByEmail(email: String): UsersRecord?
    fun verifyEmail(userId: UUID, token: String)
    fun save(user: User, hashedPassword: String, verificationToken: String, tokenExpiryDate: OffsetDateTime?)
    fun updateUser(email: String)
}