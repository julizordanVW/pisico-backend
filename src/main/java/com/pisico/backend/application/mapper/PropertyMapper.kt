package com.pisico.backend.application.mapper

import com.pisico.backend.application.dto.PropertiesResponseDto
import com.pisico.backend.domain.entities.Property
import org.mapstruct.Mapper


@Mapper(componentModel = "spring")
interface PropertyMapper {
    fun toPropertyResponseDto(property: List<Property>): List<PropertiesResponseDto>
}