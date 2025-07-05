package com.pisico.backend.infraestructure.`in`.impl

import com.pisico.backend.application.useCases.PropertiesGetter
import com.pisico.backend.infraestructure.`in`.PropertyController
import com.pisico.backend.infraestructure.`in`.dto.PageWrapper
import com.pisico.backend.infraestructure.`in`.dto.PropertiesResponse
import com.pisico.backend.infraestructure.`in`.dto.PropertyFiltersRequest
import com.pisico.backend.infraestructure.mapper.FiltersMapper
import com.pisico.backend.infraestructure.mapper.PropertyMapper
import org.springframework.web.bind.annotation.RestController

@RestController
class PropertyControllerImpl(
    private val propertiesGetter: PropertiesGetter,
    private val filtersMapper: FiltersMapper,
    private val propertyMapper: PropertyMapper
) : PropertyController {

    override fun getAllProperties(filters: PropertyFiltersRequest,
    ): PageWrapper<PropertiesResponse> {
        val responseDtoPage = propertiesGetter.execute(filtersMapper.toPropertyFiltersDto(filters))
        val mappedContent = propertyMapper.toResponseList(responseDtoPage.content)
        
        return PageWrapper(
            content = mappedContent,
            hasNext = responseDtoPage.hasNext,
            pageNumber = responseDtoPage.pageNumber
        )
    }
}
