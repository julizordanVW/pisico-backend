package com.pisico.backend.infraestructure.`in`.controller.auth

import com.pisico.backend.infraestructure.`in`.dto.auth.LoginByEmailRequest
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@RequestMapping("auth/login")
interface LoginController {
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Tag(name = "Auth", description = "Endpoints related to authentication management")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User authentication successful"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad Request - Invalid input data",
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized user",
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
                responseCode = "415",
                description = "Unsupported Media Type - Invalid content type",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error, something went wrong",
            )
        ]
    )


    fun login(
        @RequestBody request: LoginByEmailRequest
    ): ResponseEntity<Map<String, Any>>
}