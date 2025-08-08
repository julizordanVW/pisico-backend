package com.pisico.backend.infraestructure.`in`.controller.user

import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@RequestMapping("/user/me")
interface GetCurrentUserController {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Tag(name = "Users", description = "Endpoints related to user management")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Response with user details retrieved successfully"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Not valid request parameters",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not valid endpoint",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error, something went wrong",
            )
            //TODO: Add more response codes and descriptions as needed
        ]
    )
    fun getMe()
}