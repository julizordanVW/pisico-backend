package com.pisico.backend.infraestructure.mapper

import com.pisico.backend.application.dto.PropertyFiltersDto
import com.pisico.backend.infraestructure.`in`.dto.PropertyFiltersRequest
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface FiltersMapper {
    fun toPropertyFiltersDto(request: PropertyFiltersRequest): PropertyFiltersDto
}