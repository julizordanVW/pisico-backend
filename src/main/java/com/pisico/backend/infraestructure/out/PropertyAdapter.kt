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
                    address = record.get(PROPERTIES.ADDRESS),
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

            filters.minPrice?.let { PROPERTIES.PRICE.greaterOrEqual(it) },
            filters.maxPrice?.let { PROPERTIES.PRICE.lessOrEqual(it) },

            filters.minRooms?.let { PROPERTIES.ROOMS.greaterOrEqual(it) },
            filters.maxRooms?.let { PROPERTIES.ROOMS.lessOrEqual(it) },
            
            filters.roommates?.let { PROPERTIES.ROOMMATES.eq(it) })
    }


}