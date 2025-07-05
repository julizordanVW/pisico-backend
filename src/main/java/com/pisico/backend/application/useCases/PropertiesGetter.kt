package com.pisico.backend.application.useCases

import com.pisico.backend.application.dto.AddressResponseDto
import com.pisico.backend.application.dto.CoordinatesResponseDto
import com.pisico.backend.application.dto.PropertiesResponseDto
import com.pisico.backend.application.dto.PropertyFiltersDto
import com.pisico.backend.infraestructure.`in`.dto.PageWrapper
import com.pisico.backend.application.ports.out.PropertiesRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class PropertiesGetter(
    private val propertiesRepository: PropertiesRepository
) {
    fun execute(
        filters: PropertyFiltersDto,
    ): PageWrapper<PropertiesResponseDto> {
        val pageable = Pageable.unpaged()
        val list = propertiesRepository.findByFilters(filters, pageable)
            .map { property -> 
                PropertiesResponseDto(
                    id = property.id,
                    name = property.name,
                    description = property.description ?: "",
                    type = property.type,
                    price = property.price,
                    rooms = property.rooms,
                    roommates = property.roommates ?: 0,
                    furnished = property.furnished,
                    address = AddressResponseDto(
                        streetAddress = property.address.address,
                        city = property.address.city,
                        postalCode = property.address.postalCode,
                        country = property.address.country
                    ),
                    coordinates = if (property.coordinates != null) {
                        CoordinatesResponseDto(
                            latitude = property.coordinates.latitude.toString(),
                            longitude = property.coordinates.longitude.toString()
                        )
                    } else null
                )
            }

        return PageWrapper(
            content = list,
            hasNext = false,
            pageNumber = 0
        )
    }
}
