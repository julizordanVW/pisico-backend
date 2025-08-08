package com.pisico.backend.domain.entities

import java.time.LocalDate
import java.util.UUID

data class User(
    val id: UUID?,
    val name: String,
    val description: String?,
    val dateOfBirth: LocalDate?,
    val email: String?,
    val password: String,
    val emailVerified: Boolean = false,
    val verification_token: String? = null,
    val token_expiry_date: LocalDate? = null,
    val phoneNumber: String?,
    val profilePictureUrl: String?,
    val gender: Gender,
    val role : String?,
    val accountStatus: String?,
    val timeZone: String?
)

enum class Gender(val value: String) {
    WOMAN("woman"),
    MAN("man"),
    OTHER("other"),
    NOT_SPECIFIED("not_specified");
}