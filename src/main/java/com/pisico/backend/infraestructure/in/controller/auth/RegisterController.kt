package com.pisico.backend.infraestructure.`in`.controller.auth

import com.pisico.backend.infraestructure.`in`.dto.auth.RegisterByEmailRequest
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@RequestMapping("auth/register")
interface RegisterController {
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Tag(name = "Auth", description = "Endpoints related to authentication management")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "User registration successfully"
            ),
            ApiResponse(
                responseCode = "400",
                description = "An account with that email already exists",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not valid endpoint",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error, something went wrong",
            )
        ]
    )


    fun register(
        @RequestBody request: RegisterByEmailRequest
    ): ResponseEntity<Map<String, Any>>
}