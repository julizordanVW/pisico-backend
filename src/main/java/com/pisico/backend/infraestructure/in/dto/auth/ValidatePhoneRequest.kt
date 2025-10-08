package com.pisico.backend.infraestructure.`in`.dto.auth

data class ValidatePhoneRequest(
    val phoneNumber: String,
    val code: String
)