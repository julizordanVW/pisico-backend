package com.pisico.backend.infraestructure.`in`

import com.pisico.backend.config.AbstractIntegrationTest
import com.pisico.backend.infraestructure.`in`.dto.ResendEmailRequest
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
        scripts = ["classpath:db.test.scripts/resend_email_data.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    ),
    Sql(
        statements = ["delete from users"],
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
)
class ResendEmailControllerIT : AbstractIntegrationTest() {

    @LocalServerPort
    var port: Int = 0

    private lateinit var url: String

    @BeforeEach
    fun setUp() {
        port.also {
            RestAssured.port = it
            url = "http://localhost:$it/auth/verify/resend"
        }
    }

    @Test
    fun `should resend verification email successfully and return 200`() {
        val request = ResendEmailRequest("unverified@example.com")

        given()
            .contentType(ContentType.JSON)
            .`when`()
            .body(request)
            .post(url)
            .then()
            .statusCode(200)
            .body("message", Matchers.equalTo("Verification email sent successfully"))
    }

    @Test
    fun `should return 404 when user not found`() {
        val request = ResendEmailRequest("nonexistent@example.com")

        given()
            .contentType(ContentType.JSON)
            .`when`()
            .body(request)
            .post(url)
            .then()
            .statusCode(404)
            .body("message", Matchers.equalTo("User not found"))
    }

    @Test
    fun `should return 400 when email already verified`() {
        val request = ResendEmailRequest("verified@example.com")

        given()
            .contentType(ContentType.JSON)
            .`when`()
            .body(request)
            .post(url)
            .then()
            .statusCode(400)
            .body("message", Matchers.equalTo("Email already verified"))
    }

    @Test
    fun `should return 429 when trying to resend too soon`() {
        val request = ResendEmailRequest("recent@example.com")

        given()
            .contentType(ContentType.JSON)
            .`when`()
            .body(request)
            .post(url)
            .then()
            .statusCode(429)
            .body("message", Matchers.equalTo("Please wait before resending"))
    }

    @Test
    fun `should return 404 when accessing wrong endpoint path`() {
        val request = ResendEmailRequest("unverified@example.com")

        given()
            .contentType(ContentType.JSON)
            .`when`()
            .body(request)
            .post("http://localhost:$port/auth/verify/resend-wrong")
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

    @Test
    fun `should return 404 when email field is blank`() {
        val request = ResendEmailRequest("")

        given()
            .contentType(ContentType.JSON)
            .`when`()
            .body(request)
            .post(url)
            .then()
            .statusCode(404)
    }
}