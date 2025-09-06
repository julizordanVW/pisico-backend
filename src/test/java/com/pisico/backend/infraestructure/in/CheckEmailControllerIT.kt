package com.pisico.backend.infraestructure.`in`

import com.pisico.backend.config.AbstractIntegrationTest
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup

@SqlGroup(
    Sql(
        scripts = ["classpath:db.test.scripts/users_data.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    ),
    Sql(
        statements = ["delete from users"],
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
)
class CheckEmailControllerIT : AbstractIntegrationTest() {

    @LocalServerPort
    var port: Int = 0

    private lateinit var url: String

    @BeforeEach
    fun setUp() {
        port.also {
            RestAssured.port = it
            url = "http://localhost:$it/auth/check-email"
        }
    }

    // === SUCCESSFUL CHECK ===

    @Test
    fun `should return 200 and true when email exists`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("email", "test@example.com")
            .`when`()
            .post(url)
            .then()
            .log().all()
            .statusCode(200)
            .body("exists", Matchers.equalTo(true))
    }

    @Test
    fun `should return 200 and false when email does not exist`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("email", "nonexistent@example.com")
            .`when`()
            .post(url)
            .then()
            .log().all()
            .statusCode(200)
            .body("exists", Matchers.equalTo(false))
    }

    // === VALIDATION ERROR TESTS ===

    @Test
    fun `should return 400 when email format is invalid`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("email", "invalid-email")
            .`when`()
            .post(url)
            .then()
            .log().all()
            .statusCode(400)
            .body("message", Matchers.equalTo("Invalid email format."))
    }

    @Test
    fun `should return 400 when email query parameter is missing`() {
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .post(url)
            .then()
            .log().all()
            .statusCode(400)
    }

    // === HTTP ERROR TESTS ===

    @Test
    fun `should return 405 when using GET method instead of POST`() {
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .get(url)
            .then()
            .log().all()
            .statusCode(405)
    }

    @Test
    fun `should return 405 when using PUT method instead of POST`() {
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .put(url)
            .then()
            .log().all()
            .statusCode(405)
    }

//    @Test
//    fun `should return 415 when content type is not JSON`() {
//        val request = "email=test@example.com"
//
//        given()
//            .contentType(ContentType.URLENC)
//            .queryParam(request)
//            .`when`()
//            .post(url)
//            .then()
//            .statusCode(415)
//    }

    @Test
    fun `should return 404 for non-existent endpoint`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("email", "test@example.com")
            .`when`()
            .post("http://localhost:$port/auth/check-email/nonexistent")
            .then()
            .log().all()
            .statusCode(404)
    }

    // === EDGE CASE TESTS ===

    @Test
    fun `should return false when email has different case`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("email", "TEST@EXAMPLE.COM")
            .`when`()
            .post(url)
            .then()
            .log().all()
            .statusCode(200)
            .body("exists", Matchers.equalTo(false))
    }

    @Test
    fun `should return false when email has extra whitespace`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("email", " test@example.com ")
            .`when`()
            .post(url)
            .then()
            .log().all()
            .statusCode(200)
            .body("exists", Matchers.equalTo(false))
    }
}