package com.pisico.backend.infraestructure.`in`.dto

import com.pisico.backend.domain.entities.PropertyType
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import kotlin.collections.mutableListOf

data class PropertyFiltersRequest(
    @Schema(
        description = "City to filter properties",
        example = "Madrid",
        required = true
    )
    var city: String = "Madrid",

    @Schema(
        description = "Type of property to filter",
        allowableValues = ["ROOM", "APARTMENT", "HOUSE", "STUDIO", "PENTHOUSE", "DUPLEX"],
        required = true
    )
    var propertyType: PropertyType = PropertyType.APARTMENT,

    @Schema(
        description = "Postal code to filter properties",
        example = "28001"
    )
    var postalCode: String? = null,

    @Schema(
        description = "Country to filter properties",
        example = "Spain"
    )
    var country: String? = null,

    @Schema(
        description = "Minimum price to filter properties",
        example = "500"
    )
    var minPrice: BigDecimal? = null,
    @Schema(
        description = "Maximum price to filter properties",
        example = "2000"
    )
    var maxPrice: BigDecimal? = null,

    @Schema(
        description = "List of number of rooms to filter properties",
        example = "1,2,3"
    )
    var rooms:  List<Int> = mutableListOf(),

    @Schema(
        description = "Number of roommates to filter properties",
        example = "3"
    )
    var roommates: Int? = null,
)