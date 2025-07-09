package com.pisico.backend.application.dto

import com.pisico.backend.domain.entities.PropertyType
import java.math.BigDecimal

//TODO : METER la paginacion y el sorting
data class PropertyFiltersDto (
    var city: String = "Madrid",
    var postalCode: String? = null,
    var country: String? = null,

    var propertyType: PropertyType = PropertyType.APARTMENT,
    
    var minPrice: BigDecimal? = null,
    var maxPrice: BigDecimal? = null,

    var rooms : List<Int> = emptyList(),

    var roommates: Int? = null,
)