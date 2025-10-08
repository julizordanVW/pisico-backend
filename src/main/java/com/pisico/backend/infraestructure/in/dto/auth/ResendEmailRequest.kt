package com.pisico.backend.infraestructure.`in`.dto.auth

import jakarta.validation.constraints.NotBlank

data class ResendEmailRequest(
    @field:NotBlank(message = "Email is required")
    val email: String
)