package com.pisico.backend.infraestructure.`in`

import com.pisico.backend.config.AbstractIntegrationTest
import com.pisico.backend.infraestructure.`in`.dto.auth.RegisterByEmailRequest
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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
class RegisterControllerIT : AbstractIntegrationTest() {

    @LocalServerPort
    var port: Int = 0

    private lateinit var url: String

    @BeforeEach
    fun setUp() {
        port.also {
            RestAssured.port = it
            url = "http://localhost:$it/auth/register"
        }
    }

    // === SUCCESSFUL REGISTRATION TESTS ===

    @ParameterizedTest
    @CsvSource(
        "test@gmail.com, test, test, Test123!",
        "test1@gmail.com, test1, test1, Test123!",
        "test2@gmail.com, test2, test2, Test123!",
    )
    fun `should return 200 when user registration was successful`(
        email: String,
        firstName: String,
        lastName: String,
        password: String
    ) {
        val request = RegisterByEmailRequest(email, firstName, lastName, password)

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(201)
            .body("message", Matchers.equalTo("User registered successfully"))
    }


    // === VALIDATION ERROR TESTS ===

    @Test
    @Sql(statements = ["insert into users (email, name, password_hash) values ('test@gmail.com', 'test', 'Test123!')"])
    fun `should return 400 when an email already exists`() {
        val request = RegisterByEmailRequest(
            email = "test@gmail.com",
            firstName = "test",
            lastName = "test",
            password = "Test123!"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(400)
            .body("message", Matchers.equalTo("An account with that email already exists."))
    }

    @Test
    fun `should return 400 when email format is invalid`() {
        val request = RegisterByEmailRequest(
            email = "invalid_email.com",
            firstName = "test",
            lastName = "test",
            password = "Test123!"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(400)
            .body("message", Matchers.equalTo("Invalid email format."))
    }

    @Test
    fun `should return 400 when password format is invalid`() {
        val request = RegisterByEmailRequest(
            email = "test@gmail.com",
            firstName = "test",
            lastName = "test",
            password = "short"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(400)
            .body("message", Matchers.equalTo("Password must be at least 8 characters long, contain uppercase and lowercase letters, a digit, and a special character."))
    }

    // === HTTP ERROR TESTS ===

    @Test
    fun `should return 404 for non-existent endpoint`() {
        val request = RegisterByEmailRequest(
            email = "test@gmail.com",
            firstName = "test",
            lastName = "test",
            password = "Test123!"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post("http://localhost:$port/auth/register/nonexistent")
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
    fun `should return 415 when content type is not JSON`() {
        val request = "email=test@example.com&firstName=test&lastName=test&password=password"

        given()
            .contentType(ContentType.URLENC)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(415)
    }

    // === SECURITY TESTS ===

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

    @Test
    fun `should return 400 when email is null`() {
        val request = mapOf(
            "password" to "password",
            "firstName" to "test",
            "lastName" to "test"
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
            "email" to "test@example.com",
            "firstName" to "test",
            "lastName" to "test"
        )

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(400)
    }
}