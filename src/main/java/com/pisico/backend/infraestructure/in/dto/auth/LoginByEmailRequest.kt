package com.pisico.backend.infraestructure.`in`.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginByEmailRequest(

    @field:NotBlank
    @field:Email
    var email: String,

    @field:NotBlank
    @field:Size(min = 8)
    var password: String,
)