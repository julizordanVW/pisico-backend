package com.pisico.backend.infraestructure.`in`

import com.pisico.backend.config.AbstractIntegrationTest
import com.pisico.backend.domain.entities.PropertyType
import com.pisico.backend.infraestructure.`in`.controller.PropertyController
import com.pisico.backend.infraestructure.`in`.dto.PropertyFiltersRequest
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.jdbc.Sql
import kotlin.test.Test

@Sql(
    scripts = ["classpath:db.test.scripts/properties_data.sql"],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
open class PropertyControllerIT : AbstractIntegrationTest() {

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

    @ParameterizedTest
    @CsvSource(
        "Sevilla, apartment, 4",
        "Madrid, apartment, 0",
        "Barcelona, house, 0"
    )
    fun `should return correct count for different filters`(
        city: String,
        propertyType: String,
        expectedCount: Int
    ) {
        val filters = PropertyFiltersRequest(
            propertyType = PropertyType.valueOf(propertyType.uppercase()),
            city = city
        )

        val result = propertyController.getAllProperties(filters)
        assertThat(result.content).hasSize(expectedCount)
    }


    @Test
    fun `should return properties filtered by city`() {
        given()
            .queryParam("city", "Sevilla")
            .`when`()
            .get(url)
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(4))
    }

    @Test
    fun `should return properties filtered by postalCode`() {
        given()
            .queryParam("city", "Sevilla")
            .queryParam("postalCode", "41010")
            .`when`()
            .get(url)
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(4))
    }

    @Test
    fun `should return properties filtered by country`() {
        given()
            .queryParam("city", "Sevilla")
            .queryParam("country", "España")
            .`when`()
            .get(url)
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(4))
    }

    @Test
    fun `should return properties filtered by minPrice`() {
        given()
            .queryParam("city", "Sevilla")
            .queryParam("minPrice", 700)
            .`when`()
            .get(url)
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(2))
    }

    @Test
    fun `should return properties filtered by maxPrice`() {
        given()
            .queryParam("city", "Sevilla")
            .queryParam("maxPrice", 700)
            .`when`()
            .get(url)
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(3))
    }

    @Test
    fun `should return properties filtered by propertyType`() {
        given()
            .queryParam("city", "Sevilla")
            .queryParam("propertyType", "APARTMENT")
            .`when`()
            .get(url)
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(4))
    }

    @Test
    fun `should return properties filtered by rooms`() {
        given()
            .queryParam("city", "Sevilla")
            .queryParam("rooms", 1)
            .`when`()
            .get(url)
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(1))
    }

    @Test
    fun `should return properties filtered by list of rooms`() {
        given()
            .queryParam("city", "Sevilla")
            .queryParam("rooms", "1,2")
            .`when`()
            .get(url)
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(2))
    }

    @Test
    fun `should return properties filtered by roommates`() {
        given()
            .queryParam("city", "Sevilla")
            .queryParam("roommates", 2)
            .`when`()
            .get(url)
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(1))
            .body("content[0].name", containsString("Betis"))
            .body("content[0].price", equalTo(850.0F))
    }

    @Test
    fun `should return correct property when all filters are applied`() {
        given()
            .queryParam("city", "Sevilla")
            .queryParam("postalCode", "41010")
            .queryParam("country", "España")
            .queryParam("propertyType", "APARTMENT")
            .queryParam("minPrice", 700)
            .queryParam("maxPrice", 800)
            .queryParam("rooms", 2)
            .queryParam("roommates", 1)
            .`when`()
            .get(url)
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(1))
            .body("content[0].name", containsString("San Jacinto"))
    }

    @Test
    fun `should return 0 properties when filters applied doesnt match results`() {
        given()
            .queryParam("city", "Sevilla")
            .queryParam("postalCode", "41010")
            .queryParam("country", "España")
            .queryParam("propertyType", "APARTMENT")
            .queryParam("minPrice", 700)
            .queryParam("maxPrice", 800)
            .queryParam("rooms", 2)
            .queryParam("roommates", 3)
            .`when`()
            .get(url)
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(0))
    }

    @Test
    fun `should return 400 when propertyType is invalid`() {
        given()
            .queryParam("city", "Sevilla")
            .queryParam("propertyType", "INVALID_TYPE")
            .`when`()
            .get(url)
            .then()
            .statusCode(400)
            .body("message", containsString("Failed to convert property value of type 'java.lang.String'" +
                    " to required type 'com.pisico.backend.domain.entities.PropertyType"))

    }

    @Test
    fun `should return 400 when minPrice is greater than maxPrice`() {
        given()
            .queryParam("minPrice", 1000)
            .queryParam("maxPrice", 500)
            .`when`()
            .get(url)
            .then()
            .statusCode(400)
            .body("message", containsString("Min price must be less than or equal to max price."))
    }

    @Test
    fun `should return 400 when maxPrice is less than minPrice `() {
        given()
            .queryParam("minPrice", 500)
            .queryParam("maxPrice", 100)
            .`when`()
            .get(url)
            .then()
            .statusCode(400)
            .body("message", containsString("Min price must be less than or equal to max price."))
    }

    @Test
    fun `should return 400 when numeric parameter is not a number`() {
        given()
            .queryParam("rooms", "notANumber")
            .`when`()
            .get(url)
            .then()
            .statusCode(400)
    }

    @Test
    fun `should return 404 for non-existent endpoint`() {
        given()
            .`when`()
            .get("http://localhost:$port/properties/hola")
            .then()
            .statusCode(404)
    }

}