package com.pisico.backend.application.dto

import com.pisico.backend.domain.entities.PropertyType
import java.math.BigDecimal
import kotlin.collections.mutableListOf

//TODO : METER la paginacion y el sorting
data class PropertyFiltersDto(
    var city: String? = "Madrid",
    var propertyType: PropertyType? = PropertyType.APARTMENT,
    var postalCode: String? = null,
    var country: String? = null,
    var minPrice: BigDecimal? = null,
    var maxPrice: BigDecimal? = null,
    var rooms: List<Int> = mutableListOf(),
    var roommates: Int? = null
)