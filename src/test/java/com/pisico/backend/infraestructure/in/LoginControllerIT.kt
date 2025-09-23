package com.pisico.backend.infraestructure.`in`

import com.pisico.backend.config.AbstractIntegrationTest
import com.pisico.backend.infraestructure.`in`.dto.LoginByEmailRequest
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
class LoginControllerIT : AbstractIntegrationTest() {

    @LocalServerPort
    var port: Int = 0

    private lateinit var url: String

    @BeforeEach
    fun setUp() {
        port.also {
            RestAssured.port = it
            url = "http://localhost:$it/auth/login"
        }
    }

    // === SUCCESSFUL LOGIN TESTS ===

    @Test
    fun `should return 200 and success message when login is successful`() {
        val request = LoginByEmailRequest(
            email = "test@example.com",
            password = "password"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(200)
            .body("message", Matchers.equalTo("Login successful."))
    }

    @Test
    fun `should return 200 when admin user logs in successfully`() {
        val request = LoginByEmailRequest(
            email = "admin@example.com",
            password = "password"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(200)
            .body("message", Matchers.equalTo("Login successful."))
    }

    @Test
    fun `should return 200 when user2 logs in successfully`() {
        val request = LoginByEmailRequest(
            email = "user2@example.com",
            password = "password"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(200)
            .body("message", Matchers.equalTo("Login successful."))
    }

    // === AUTHENTICATION FAILURE TESTS ===

    @Test
    fun `should return 401 when email is incorrect`() {
        val request = LoginByEmailRequest(
            email = "wrong@example.com",
            password = "password"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Invalid email or password."))
    }

    @Test
    fun `should return 401 when password is incorrect`() {
        val request = LoginByEmailRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Invalid email or password."))
    }

    @Test
    fun `should return 401 when both email and password are incorrect`() {
        val request = LoginByEmailRequest(
            email = "nonexistent@example.com",
            password = "wrongpassword"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Invalid email or password."))
    }

    @Test
    fun `should return 401 when email exists but password is empty`() {
        val request = LoginByEmailRequest(
            email = "test@example.com",
            password = ""
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Invalid email or password."))
    }

    // === VALIDATION ERROR TESTS ===

    @Test
    fun `should return 400 when email is null`() {
        val request = mapOf(
            "password" to "password"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(400)
    }

    @Test
    fun `should return 400 when password is null`() {
        val request = mapOf(
            "email" to "test@example.com"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(400)
    }

    @Test
    fun `should return 400 when request body is empty`() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .`when`()
            .post(url)
            .then()
            .statusCode(400)
    }

    // === EDGE CASE TESTS ===

    @Test
    fun `should return 401 when email has different case but password is correct`() {
        val request = LoginByEmailRequest(
            email = "TEST@EXAMPLE.COM",
            password = "password"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Invalid email or password."))
    }

    @Test
    fun `should return 401 when password has extra whitespace`() {
        val request = LoginByEmailRequest(
            email = "test@example.com",
            password = " password "
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Invalid email or password."))
    }

    @Test
    fun `should return 401 when email has extra whitespace`() {
        val request = LoginByEmailRequest(
            email = " test@example.com ",
            password = "password"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Invalid email or password."))
    }

    @Test
    fun `should handle special characters in email correctly`() {
        val request = LoginByEmailRequest(
            email = "test+special@example.com",
            password = "password"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Invalid email or password."))
    }

    // === INACTIVE USER TEST ===

    @Test
    fun `should return 401 for inactive user`() {
        val request = LoginByEmailRequest(
            email = "inactive@example.com",
            password = "password"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Account is not active."))
    }

    // === HTTP ERROR TESTS ===

    @Test
    fun `should return 404 for non-existent endpoint`() {
        given()
            .contentType(ContentType.JSON)
            .body(LoginByEmailRequest("test@example.com", "password"))
            .`when`()
            .post("http://localhost:$port/auth/login/nonexistent")
            .then()
            .statusCode(404)
    }

    @Test
    fun `should return 415 when content type is not JSON`() {
        val request = "email=test@example.com&password=password"

        given()
            .contentType(ContentType.URLENC)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(415)
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
    fun `should return 405 when using PUT method instead of POST`() {
        given()
            .contentType(ContentType.JSON)
            .body(LoginByEmailRequest("test@example.com", "password"))
            .`when`()
            .put(url)
            .then()
            .statusCode(405)
    }

    // === SECURITY TESTS ===

    @Test
    fun `should not reveal user existence in error message for non-existent email`() {
        val request = LoginByEmailRequest(
            email = "definitely-does-not-exist@example.com",
            password = "password"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Invalid email or password."))
    }

    @Test
    fun `should handle very long email addresses gracefully`() {
        val longEmail = "a".repeat(100) + "@example.com"
        val request = LoginByEmailRequest(
            email = longEmail,
            password = "password"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Invalid email or password."))
    }

    @Test
    fun `should handle very long passwords gracefully`() {
        val longPassword = "a".repeat(1000)
        val request = LoginByEmailRequest(
            email = "test@example.com",
            password = longPassword
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Invalid email or password."))
    }

    // === SQL INJECTION TESTS ===

    @Test
    fun `should handle potential SQL injection in email field`() {
        val request = LoginByEmailRequest(
            email = "'; DROP TABLE users; --",
            password = "password"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Invalid email or password."))
    }

    @Test
    fun `should handle potential SQL injection in password field`() {
        val request = LoginByEmailRequest(
            email = "test@example.com",
            password = "' OR '1'='1"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Invalid email or password."))
    }

    // === UNICODE AND SPECIAL CHARACTER TESTS ===

    @Test
    fun `should handle unicode characters in password`() {
        val request = LoginByEmailRequest(
            email = "test@example.com",
            password = "pässwörd™"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(401)
            .body("message", Matchers.equalTo("Invalid email or password."))
    }

    @Test
    fun `should handle empty request body`() {
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .post(url)
            .then()
            .statusCode(400)
    }
}