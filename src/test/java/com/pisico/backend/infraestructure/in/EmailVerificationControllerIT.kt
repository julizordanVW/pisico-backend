package com.pisico.backend.infraestructure.`in`

import com.pisico.backend.config.AbstractIntegrationTest
import com.pisico.backend.infraestructure.`in`.dto.auth.VerifyEmailRequest
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
class EmailVerificationControllerIT : AbstractIntegrationTest() {

    @LocalServerPort
    var port: Int = 0

    private lateinit var url: String

    @BeforeEach
    fun setUp() {
        port.also {
            RestAssured.port = it
            url = "http://localhost:$it/auth/verify"
        }
    }

    @Test
    fun `should verify email successfully with valid token and return 200`() {
        val request = VerifyEmailRequest("verification-token-123")
        
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .body(request)
            .post(url)
            .then()
            .statusCode(200)
            .body("success", Matchers.equalTo(true))
            .body("message", Matchers.equalTo("Email verified successfully."))
    }

    @Test
    fun `should return 400 when verifying with non-existing token`() {
        val request = VerifyEmailRequest("xTokenThatDoesNotExistx")

        given()
            .contentType(ContentType.JSON)
            .`when`()
            .body(request)
            .post(url)
            .then()
            .statusCode(400)
            .body("success", Matchers.equalTo(false))
            .body("message", Matchers.equalTo("Invalid or expired verification token"))
    }

    @Test
    fun `should return 400 when verifying with expired token`() {
        val request = VerifyEmailRequest("verification-token-1234")

        given()
            .contentType(ContentType.JSON)
            .`when`()
            .body(request)
            .post(url)
            .then()
            .statusCode(400)
            .body("success", Matchers.equalTo(false))
            .body("message", Matchers.equalTo("Token expired."))
    }

    @Test
    fun `should return 400 when verifying already verified email`() {
        val request = VerifyEmailRequest("verification-token-12345")

        given()
            .contentType(ContentType.JSON)
            .`when`()
            .body(request)
            .post(url)
            .then()
            .statusCode(400)
            .body("success", Matchers.equalTo(false))
            .body("message", Matchers.equalTo("Email already verified"))
    }

    @Test
    fun `should return 404 when accessing wrong endpoint path`() {
        val request = VerifyEmailRequest("verification-token-123")

        given()
            .contentType(ContentType.JSON)
            .`when`()
            .body(request)
            .post("http://localhost:$port/auth/verify-wrong")
            .then()
            .statusCode(404)
    }

    @Test
    fun `should return 405 when using GET method instead of POST`() {
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .get(url)
            .then()
            .statusCode(405)
    }

    @Test
    fun `should return 400 when request body is empty`() {
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .body("")
            .post(url)
            .then()
            .statusCode(400)
    }
}
