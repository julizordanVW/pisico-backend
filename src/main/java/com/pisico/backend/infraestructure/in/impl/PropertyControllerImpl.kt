package com.pisico.backend.infraestructure.`in`.impl

import com.pisico.backend.application.useCases.PropertiesRetriever
import com.pisico.backend.infraestructure.`in`.PropertyController
import com.pisico.backend.infraestructure.`in`.dto.PageWrapper
import com.pisico.backend.infraestructure.`in`.dto.PropertiesResponse
import com.pisico.backend.infraestructure.`in`.dto.PropertyFiltersRequest
import com.pisico.backend.infraestructure.mapper.FiltersMapper
import com.pisico.backend.infraestructure.mapper.PropertiesApiMapper
import org.springframework.web.bind.annotation.RestController

@RestController
class PropertyControllerImpl(
    private val propertiesRetriever: PropertiesRetriever,
    private val filtersMapper: FiltersMapper,
    private val propertiesApiMapper: PropertiesApiMapper
) : PropertyController {

    override fun getAllProperties(filters: PropertyFiltersRequest,
    ): PageWrapper<PropertiesResponse> {
        val response = propertiesRetriever.execute(filtersMapper.toPropertyFiltersDto(filters))
        val mappedContent = propertiesApiMapper.toResponseList(response)
        
        return PageWrapper(
            content = mappedContent,
            hasNext = false,
            pageNumber = 0
        )
    }
}