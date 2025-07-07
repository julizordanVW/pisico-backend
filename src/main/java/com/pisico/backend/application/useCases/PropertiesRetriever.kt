package com.pisico.backend.application.useCases

import com.pisico.backend.application.dto.PropertiesResponseDto
import com.pisico.backend.application.dto.PropertyFiltersDto
import com.pisico.backend.application.ports.out.PropertiesRepository
import com.pisico.backend.application.mapper.PropertyMapper
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class PropertiesRetriever(
    private val propertiesRepository: PropertiesRepository,
    private val propertyMapper: PropertyMapper
) {
    fun execute(
        filters: PropertyFiltersDto,
    ): List<PropertiesResponseDto> {
        val pageable = Pageable.unpaged()
        val propertyEntities = propertiesRepository.findByFilters(filters, pageable)
        return propertyMapper.toPropertyResponseDto(propertyEntities)
    }
}
