package com.pisico.backend.infraestructure.`in`

import com.pisico.backend.application.dto.PropertiesResponseDto
import com.pisico.backend.application.dto.PropertyFiltersDto
import com.pisico.backend.infraestructure.`in`.dto.PageWrapper
import com.pisico.backend.infraestructure.`in`.dto.PropertiesResponse
import com.pisico.backend.infraestructure.`in`.dto.PropertyFiltersRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@RequestMapping("/properties")
interface PropertyController {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Retrieve properties with its attributes")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "List of properties with details retrieved successfully"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Not valid request parameters",
            )
            //TODO: Add more response codes and descriptions as needed
        ]
    )
    
    fun getAllProperties(
        @ModelAttribute filters: PropertyFiltersRequest,
    ): PageWrapper<PropertiesResponse>
}