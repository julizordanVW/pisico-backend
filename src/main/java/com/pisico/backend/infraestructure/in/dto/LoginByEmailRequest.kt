package com.pisico.backend.infraestructure.`in`.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginByEmailRequest(

    @field:NotBlank
    @field:Email
    private var email: String,

    @field:NotBlank
    @field:Size(min = 8)
    private var password: String,
)