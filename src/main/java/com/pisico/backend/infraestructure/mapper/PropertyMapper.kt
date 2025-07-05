package com.pisico.backend.infraestructure.mapper

import com.pisico.backend.application.dto.PropertiesResponseDto
import com.pisico.backend.infraestructure.`in`.dto.PropertiesResponse
import org.mapstruct.Mapper


@Mapper(componentModel = "spring")
interface PropertyMapper {
    fun toResponse(dto: PropertiesResponseDto): PropertiesResponse
    fun toResponseList(dtos: List<PropertiesResponseDto>): List<PropertiesResponse>
}