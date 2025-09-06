package com.pisico.backend.infraestructure.out.dto

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class UserPersistenceDto(
    val id: UUID?,
    val name: String?,
    val description: String?,
    val dateOfBirth: LocalDate?,
    val email: String,
    val passwordHash: String,
    val emailVerified: Boolean,
    val verificationToken: String?,
    val tokenExpiryDate: OffsetDateTime?,
    val phoneNumber: String?,
    val profilePictureUrl: String?,
    val gender: String?,
    val role: String?,
    val lastLogin: OffsetDateTime?,
    val timeZone: String?,
    val accountStatus: String?,
    val rowCreatedOn: OffsetDateTime?,
    val rowUpdatedOn: OffsetDateTime?
)