package com.pisico.backend.infraestructure.out

import com.pisico.backend.application.dto.PropertyFiltersDto
import com.pisico.backend.config.AbstractIntegrationTest
import com.pisico.backend.domain.entities.PropertyType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.test.assertEquals

@Sql(
    scripts = ["classpath:db.test.scripts/properties_data.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
    statements = ["delete from properties"],
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
open class PropertyAdapterIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var propertyAdapter: PropertyAdapter
    
    @Test
    fun `findByFilters should return properties filtered by city and type`() {
        val filters = PropertyFiltersDto(
            city = "Sevilla",
            propertyType = PropertyType.APARTMENT
        )
        val properties = propertyAdapter.findByFilters(filters, Pageable.unpaged())

        assertThat(properties).hasSize(4)
        assertThat(properties).allMatch { it.address.city == "Sevilla" && it.type == PropertyType.APARTMENT }
    }

    @Test
    fun `findByFilters should return properties filtered by minPrice and maxPrice`() {
        val filters = PropertyFiltersDto(
            city = "Sevilla",
            propertyType = PropertyType.APARTMENT,
            minPrice = BigDecimal("700"),
            maxPrice = BigDecimal("800")
        )
        val properties = propertyAdapter.findByFilters(filters, Pageable.unpaged())

        assertThat(properties).hasSize(1)
        assertThat(properties[0].name).contains("San Jacinto")
    }

    @Test
    fun `findByFilters should return properties filtered by rooms`() {
        val filters = PropertyFiltersDto(
            city = "Sevilla",
            propertyType = PropertyType.APARTMENT,
            rooms = listOf(1)
        )
        val properties = propertyAdapter.findByFilters(filters, Pageable.unpaged())

        assertThat(properties).hasSize(1)
        assertThat(properties[0].name).isEqualTo("Estudio en Alfarería")
    }

    @Test
    fun `findByFilters should return properties filtered by multiple rooms`() {
        val filters = PropertyFiltersDto(
            city = "Sevilla",
            propertyType = PropertyType.APARTMENT,
            rooms = listOf(1, 2)
        )
        val properties = propertyAdapter.findByFilters(filters, Pageable.unpaged())

        assertThat(properties).hasSize(2)
        assertThat(properties[0].name).contains("Alfarería")
        assertThat(properties[1].name).contains("San Jacinto")
    }

    @Test
    fun `findByFilters should return properties filtered by multiple rooms including 4+`() {
        val filters = PropertyFiltersDto(
            city = "Sevilla",
            propertyType = PropertyType.APARTMENT,
            rooms = listOf(1, 4)
        )
        val properties = propertyAdapter.findByFilters(filters, Pageable.unpaged())

        assertThat(properties).hasSize(2)
        assertEquals(properties[0].rooms, 1)
        assertEquals(properties[0].name, "Estudio en Alfarería")
        assertEquals(properties[1].rooms, 4)
        assertEquals(properties[1].name, "Apartamento en Triana")
    }


    @Test
    fun `findByFilters should correctly map all property fields`() {
        val filters = PropertyFiltersDto(
            city = "Sevilla",
            propertyType = PropertyType.APARTMENT,
            rooms = listOf(2)
        )
        val properties = propertyAdapter.findByFilters(filters, Pageable.unpaged())

        assertThat(properties).hasSize(1)
        val property = properties[0]
        assertThat(property.id).isNotNull()
        assertThat(property.name).isEqualTo("Apartamento Triana San Jacinto")
        assertThat(property.description).isEqualTo("Apartamento moderno para estudiantes")
        assertThat(property.type).isEqualTo(PropertyType.APARTMENT)
        assertThat(property.price).isEqualTo(BigDecimal("700.00"))
        assertThat(property.rooms).isEqualTo(2)
        assertThat(property.bathrooms).isEqualTo(1)
        assertThat(property.roommates).isEqualTo(1)
        assertThat(property.furnished).isTrue()
        assertThat(property.address.streetAddress).isEqualTo("Calle San Jacinto 102")
        assertThat(property.address.city).isEqualTo("Sevilla")
        assertThat(property.address.postalCode).isEqualTo("41010")
        assertThat(property.address.country).isEqualTo("España")
        assertThat(property.coordinates).isNotNull()
        assertThat(property.coordinates?.latitude).isEqualTo("37.3861")
        assertThat(property.coordinates?.longitude).isEqualTo("-6.0024")
    }

}
