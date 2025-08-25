package com.pisico.backend.domain.entities

import java.time.LocalDate
import java.util.UUID
import java.util.regex.Pattern

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
    val role: String?,
    val accountStatus: String?,
    val timeZone: String?
) {
    companion object {

        fun isValidEmail(email: String): Boolean {
            val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z]{2,6})+\$"
            return Pattern.compile(emailRegex)
                .matcher(email)
                .matches()
        }

        fun isValidPassword(password: String): Boolean {
            val lengthRegex = ".{8,}".toRegex()
            val uppercaseRegex = ".*[A-Z].*".toRegex()
            val lowercaseRegex = ".*[a-z].*".toRegex()
            val digitRegex = ".*[0-9].*".toRegex()
            val specialCharRegex = ".*[@#$%^&+.=!_\\-].*".toRegex()

            return password.matches(lengthRegex) &&
                    password.matches(uppercaseRegex) &&
                    password.matches(lowercaseRegex) &&
                    password.matches(digitRegex) &&
                    password.matches(specialCharRegex)
        }

    }

    enum class Gender(val value: String) {
        WOMAN("WOMAN"),
        MAN("MAN"),
        OTHER("OTHER"),
        NOT_SPECIFIED("NOT_SPECIFIED");
    }
}