package com.pisico.backend.infraestructure.`in`

import com.pisico.backend.config.PostgresTestcontainersConfiguration
import com.pisico.backend.domain.entities.PropertyType
import com.pisico.backend.infraestructure.`in`.dto.PropertyFiltersRequest
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestcontainersConfiguration::class)
@Testcontainers
@Profile("test")
class PropertyControllerIT {

    @Autowired
    lateinit var propertyController: PropertyController

    @LocalServerPort
    var port: Int = 0

    private lateinit var url: String

    @BeforeEach
    fun setUp() {
        port.also {
            RestAssured.port = it
            url = "http://localhost:$it/properties"
        }
    }

    @Test
    fun `should return 200 when getAllProperties`() {
        val filters = PropertyFiltersRequest(
            propertyType = PropertyType.APARTMENT,
            city = "Sevilla"
        )
        
        propertyController.getAllProperties(filters)

        given()
            .`when`()
            .get(url)
            .then()
            .statusCode(200)
    }
}