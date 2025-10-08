package com.pisico.backend.infraestructure.`in`.controller.auth

import com.pisico.backend.infraestructure.`in`.dto.auth.VerifyPhoneRequest
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@RequestMapping("auth/phone-verification/send")
internal interface SendPhoneVerificationController {

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Tag(name = "Auth", description = "Endpoints related to phone verification")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Verification code sent successfully"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid phone number"
            ),
            ApiResponse(
                responseCode = "429",
                description = "Too many requests"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error, something went wrong",
            )
        ]
    )
    fun sendVerificationCode(
        @RequestBody request: VerifyPhoneRequest
    ): ResponseEntity<Map<String, Any>>
}