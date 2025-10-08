package com.pisico.backend.application.useCases

data class VerificationData(
    val code: String,
    val attempts: Int,
    val createdAt: String
)