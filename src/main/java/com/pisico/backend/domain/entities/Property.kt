package com.pisico.backend.domain.entities

import java.math.BigDecimal
import java.util.UUID

data class Property(
    val id: UUID,
    val name: String,
    val description: String?,
    val type: PropertyType,
    val price: BigDecimal,
    val rooms: Int,
    val bathrooms: Int,
    val roommates: Int?,
    val furnished: Boolean,
    val address: Address,
    val coordinates: Coordinates?
)
