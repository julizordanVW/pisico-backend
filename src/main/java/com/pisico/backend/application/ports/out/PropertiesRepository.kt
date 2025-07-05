package com.pisico.backend.application.ports.out

import com.pisico.backend.application.dto.PropertyFiltersDto
import com.pisico.backend.domain.entities.Property
import org.springframework.data.domain.Pageable

interface PropertiesRepository {
    fun findByFilters(filters: PropertyFiltersDto, pageable: Pageable):  List<Property>
    fun countByFilters(filters: PropertyFiltersDto): Long
}