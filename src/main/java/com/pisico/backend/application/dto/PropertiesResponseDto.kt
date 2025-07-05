package com.pisico.backend.application.dto

import com.pisico.backend.domain.entities.PropertyType
import java.math.BigDecimal
import java.util.UUID

data class PropertiesResponseDto(
    val id: UUID,
    val name: String,
    val description: String,
    val type: PropertyType,
    val price: BigDecimal,
    val rooms: Int,
    val roommates: Int,
    val furnished: Boolean,
    val address: AddressResponseDto,
    val coordinates: CoordinatesResponseDto?
)

data class AddressResponseDto(
    val streetAddress: String,
    val city: String,
    val postalCode: String,
    val country: String
)

data class CoordinatesResponseDto(
    val latitude: String,
    val longitude: String
)