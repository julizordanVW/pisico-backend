package com.pisico.backend.domain.entities

data class UserVerification(
    val email: String,
    val emailVerified: Boolean,
)