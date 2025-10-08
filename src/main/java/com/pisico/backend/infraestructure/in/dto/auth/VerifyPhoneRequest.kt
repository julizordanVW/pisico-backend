package com.pisico.backend.infraestructure.`in`.dto.auth

data class VerifyPhoneRequest (
    val language: String,
    val phoneNumber: String,
)