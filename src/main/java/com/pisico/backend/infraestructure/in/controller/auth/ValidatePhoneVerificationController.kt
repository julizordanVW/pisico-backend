package com.pisico.backend.infraestructure.`in`.controller.auth

import com.pisico.backend.infraestructure.`in`.dto.auth.ValidatePhoneRequest
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@RequestMapping("auth/phone-verification/validate")
internal interface ValidatePhoneVerificationController {

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Tag(name = "Auth", description = "Endpoints related to phone verification")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Phone number verified successfully"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid or expired verification code"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not valid endpoint",
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
    fun validateVerificationCode(
        @RequestBody request: ValidatePhoneRequest
    ): ResponseEntity<Map<String, Any>>
}
