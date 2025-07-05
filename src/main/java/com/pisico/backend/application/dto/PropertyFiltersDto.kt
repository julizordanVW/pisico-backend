package com.pisico.backend.application.dto

import com.pisico.backend.domain.entities.PropertyType
import java.math.BigDecimal

data class PropertyFiltersDto (
    var city: String = "Madrid",
    var postalCode: String? = null,
    var country: String? = null,

    var propertyType: PropertyType = PropertyType.APARTMENT,
    
    var minPrice: BigDecimal? = null,
    var maxPrice: BigDecimal? = null,

    var minRooms: Int? = null,
    var maxRooms: Int? = null,

    var roommates: Int? = null,
)