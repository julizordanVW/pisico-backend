package com.pisico.backend.infraestructure.`in`.dto

import com.pisico.backend.domain.entities.PropertyType
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

data class PropertiesResponse(
    @Schema(name = "name", description = "Name of the property", required = true, example = "Flat in 5th Avenue")
    val name: String,
    @Schema(name = "description", description = "Description of the property", required = true, example = "Flat with 3 rooms")
    val description: String,
    @Schema(name = "type", description = "Type of the property", required = true)
    val type: PropertyType,
    @Schema(name = "price", description = "Price of the property", required = true, example = "1000")
    val price: BigDecimal,
    @Schema(name = "rooms", description = "Rooms of the property", required = true, example = "3")
    val rooms: Int,
    @Schema(name = "bathrooms", description = "Bathrooms of the property", required = true, example = "1")
    val bathrooms: Int,
    @Schema(name = "roommates", description = "Roommates of the property", example = "2")
    val roommates: Int,
    @Schema(name = "furnished", description = "Declares if the property is furnished or not", example = "false")
    val furnished: Boolean,
    @Schema(name = "address", description = "Address details of the property", required = true)
    val address: AddressResponse,
    @Schema(name = "coordinates", description = "Coordinates of the property", required = true)
    val coordinates: CoordinatesResponse
)

data class AddressResponse(
    @Schema(name = "streetAddress", description = "Street and number of the property", example = "5th Avenue, 48")
    val streetAddress: String,
    @Schema(name = "city", description = "City where the property is located", example = "New York")
    val city: String,
    @Schema(name = "postalCode", description = "Postal code of the property", example = "10001")
    val postalCode: String,
    @Schema(name = "country", description = "Country where the property is located", example = "USA")
    val country: String
)

data class CoordinatesResponse(
    @Schema(name = "latitude", description = "Latitude of the property", example = "40.7128")
    val latitude: String,
    @Schema(name = "longitude", description = "Longitude of the property", example = "-74.0060")
    val longitude: String
)