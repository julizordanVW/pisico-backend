package com.pisico.backend.infraestructure.out

import com.pisico.backend.application.dto.PropertyFiltersDto
import com.pisico.backend.application.ports.out.PropertiesRepository
import com.pisico.backend.domain.entities.Address
import com.pisico.backend.domain.entities.Coordinates
import com.pisico.backend.domain.entities.Property
import com.pisico.backend.domain.entities.PropertyType
import com.pisico.backend.jooq.generated.Tables.PROPERTIES
import org.jooq.DSLContext
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.jooq.Condition
import java.math.BigDecimal

@Repository
open class PropertyAdapter(
    private val dslContext: DSLContext
) : PropertiesRepository {

    override fun findByFilters(
        filters: PropertyFiltersDto,
        pageable: Pageable
    ): List<Property> {
        val whereConditions = getFilters(filters)

        val query = dslContext.selectFrom(PROPERTIES)
            .where(whereConditions)
            .limit(10)
            .offset(0)

        return query.fetch().map { record ->
            Property(
                id = record.get(PROPERTIES.ID),
                name = record.get(PROPERTIES.NAME),
                description = record.get(PROPERTIES.DESCRIPTION),
                type = PropertyType.fromValue(record.get(PROPERTIES.TYPE)) ?: PropertyType.APARTMENT,
                price = record.get(PROPERTIES.PRICE),
                rooms = record.get(PROPERTIES.ROOMS),
                bathrooms = record.get(PROPERTIES.BATHROOMS),
                roommates = record.get(PROPERTIES.ROOMMATES),
                furnished = record.get(PROPERTIES.FURNISHED),
                address = Address(
                    streetAddress = record.get(PROPERTIES.ADDRESS),
                    city = record.get(PROPERTIES.CITY),
                    postalCode = record.get(PROPERTIES.POSTAL_CODE),
                    country = record.get(PROPERTIES.COUNTRY)
                ),
                coordinates = if (record.get(PROPERTIES.LATITUDE) != null && record.get(PROPERTIES.LONGITUDE) != null) {
                    Coordinates(record.get(PROPERTIES.LATITUDE), record.get(PROPERTIES.LONGITUDE))
                } else null
            )
        }
    }

    override fun countByFilters(filters: PropertyFiltersDto): Long {
        TODO("Not yet implemented")
    }

    fun getFilters(filters: PropertyFiltersDto): List<Condition> {
        return listOfNotNull(
            PROPERTIES.CITY.eq(filters.city),
            PROPERTIES.TYPE.eq(filters.propertyType.value),

            filters.postalCode?.let { PROPERTIES.POSTAL_CODE.eq(it) },
            filters.country?.let { PROPERTIES.COUNTRY.eq(it) },

            buildPricesCondition(filters.minPrice, filters.maxPrice),
            
            buildRoomsCondition(filters.rooms),
            
            filters.roommates?.let { PROPERTIES.ROOMMATES.eq(it) })
    }

    private fun buildRoomsCondition(selectedRooms: List<Int>): Condition? {
        if (selectedRooms.isEmpty()) return null

        val conditions = mutableListOf<Condition>()

        if (selectedRooms.contains(4)) {
            conditions.add(PROPERTIES.ROOMS.greaterOrEqual(4))
        }

        val normalRooms = selectedRooms.filter { it in 1..3 }
        if (normalRooms.isNotEmpty()) {
            conditions.add(PROPERTIES.ROOMS.`in`(normalRooms))
        }

        return when {
            conditions.isEmpty() -> null
            conditions.size == 1 -> conditions.first()
            else -> conditions.reduce { acc, condition -> acc.or(condition) }
        }
    }

    //TODO: Cambiar a ingles y hacer que devuelva 400 no 500. Como se hacia??
    private fun buildPricesCondition(minPrice: BigDecimal?, maxPrice: BigDecimal?): Condition? {
        if (minPrice == null && maxPrice == null) return null

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw IllegalArgumentException("El precio mínimo no puede ser mayor que el precio máximo.")
        }

        if (minPrice != null && maxPrice != null && maxPrice < minPrice) {
            throw IllegalArgumentException("El precio máximo no puede ser menor que el precio mínimo.")
        }
        
        val conditions = mutableListOf<Condition>()

        if (minPrice != null) {
            conditions.add(PROPERTIES.PRICE.greaterOrEqual(minPrice))
        }

        if (maxPrice != null) {
            conditions.add(PROPERTIES.PRICE.lessOrEqual(maxPrice))
        }

        return when {
            conditions.isEmpty() -> null
            conditions.size == 1 -> conditions.first()
            else -> conditions.reduce { acc, condition -> acc.and(condition) }
        }
    }


}