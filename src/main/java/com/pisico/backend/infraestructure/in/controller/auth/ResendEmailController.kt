package com.pisico.backend.infraestructure.`in`.controller.auth

import com.pisico.backend.infraestructure.`in`.dto.ResendEmailRequest
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@RequestMapping("auth/verify/resend")
interface ResendEmailController{
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Tag(name = "Auth", description = "Endpoints related to resend a new email to an user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Email sent successfully"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Email already verified"
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
            ),
            ApiResponse(
                responseCode = "429",
                description = "Too many requests. Please wait 2 minutes"
            ),
            ApiResponse(
                responseCode = "405",
                description = "Method not Allowed",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error, something went wrong",
            )
        ]
    )

    fun resendEmail(@RequestBody request: ResendEmailRequest): ResponseEntity<Map<String, Any>>
}