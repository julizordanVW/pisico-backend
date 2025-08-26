package com.pisico.backend.infraestructure.`in`

import com.pisico.backend.config.AbstractIntegrationTest
import com.pisico.backend.infraestructure.`in`.controller.auth.RegisterController
import com.pisico.backend.infraestructure.`in`.dto.RegisterByEmailRequest
import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@Transactional
@Rollback
open class RegisterControllerIT : AbstractIntegrationTest() {

    @Autowired
    lateinit var registerController: RegisterController

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

    @ParameterizedTest
    @CsvSource(
        "test@gmail.com, test, test, Test123!",
        "test1@gmail.com, test1, test1, Test123!",
        "test2@gmail.com, test2, test2, Test123!",
    )
    fun `should 200 when user registration was successful`(
        email: String,
        firstName: String,
        lastName: String,
        password: String
    ) {
        val request = RegisterByEmailRequest(
            email = email,
            firstName = firstName,
            lastName = lastName,
            password = password
        )

        val result = registerController.register(request)
        assertThat(result.statusCode.value()).isEqualTo(200)
        assertThat(result.body?.get("message")).isEqualTo("User registered successfully")
    }


    @Test
    @Sql(statements = ["insert into users (email, name, password_hash) values ('test@gmail.com', 'test', 'Test123!')"])
    fun `should return 400 when an email already exists`() {
        val request = RegisterByEmailRequest(
            email = "test@gmail.com",
            firstName = "test",
            lastName = "test",
            password = "Test123!"
        )

        val result = registerController.register(request)
        assertThat(result.statusCode.value()).isEqualTo(400)
        assertThat(result.body?.get("message")).isEqualTo("An account with that email already exists.")
    }

    @ParameterizedTest
    @CsvSource(
        " '', 'First', 'Last', 'Pass123!', 'Invalid email format.'",
        "invalid_email.com, 'First', 'Last', 'Pass123!', 'Invalid email format.'",
        "test@@gmail.com, 'First', 'Last', 'Pass123!', 'Invalid email format.'",
        "test@.com, 'First', 'Last', 'Pass123!', 'Invalid email format.'",
        "test@gmail..com, 'First', 'Last', 'Pass123!', 'Invalid email format.'",
        "test@gmail.com., 'First', 'Last', 'Pass123!', 'Invalid email format.'"
    )
    fun `should return 400 Bad Request for invalid email format`(
        email: String?,
        firstName: String,
        lastName: String,
        password: String,
        expectedMessage: String
    ) {
        val request = RegisterByEmailRequest(
            email = email.toString(),
            firstName = firstName,
            lastName = lastName,
            password = password
        )

        val result = registerController.register(request)
        assertThat(result.statusCode.value()).isEqualTo(400)
        assertThat(result.body?.get("message")).isEqualTo(expectedMessage)
    }

    @ParameterizedTest
    @CsvSource(
        "test@gmail.com, '', 'Last', 'Pass123!', 'First name cannot be empty.'",
        "test1@gmail.com, 'First', '', 'Pass123!', 'Last name cannot be empty.'"
    )
    fun `should return 400 Bad Request for invalid first or last name`(
        email: String,
        firstName: String?,
        lastName: String?,
        password: String,
        expectedMessage: String
    ) {
        val request = RegisterByEmailRequest(
            email = email,
            firstName = firstName.toString(),
            lastName = lastName.toString(),
            password = password
        )

        val result = registerController.register(request)
        assertThat(result.statusCode.value()).isEqualTo(400)
        assertThat(result.body?.get("message")).isEqualTo(expectedMessage)
    }

    @ParameterizedTest
    @CsvSource(
        "test2@gmail.com, 'First', 'Last', 'pass', 'Password must be at least 8 characters long, contain uppercase and lowercase letters, a digit, and a special character.'",
        "test3@gmail.com, 'First', 'Last', 'PASSWORD!', 'Password must be at least 8 characters long, contain uppercase and lowercase letters, a digit, and a special character.'",
        "test4@gmail.com, 'First', 'Last', 'password!', 'Password must be at least 8 characters long, contain uppercase and lowercase letters, a digit, and a special character.'",
        "test5@gmail.com, 'First', 'Last', 'Password!', 'Password must be at least 8 characters long, contain uppercase and lowercase letters, a digit, and a special character.'",
        "test6@gmail.com, 'First', 'Last', 'Password123', 'Password must be at least 8 characters long, contain uppercase and lowercase letters, a digit, and a special character.'",
    )
    fun `should return 400 Bad Request for invalid password format`(
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        expectedMessage: String
    ) {
        val request = RegisterByEmailRequest(
            email = email,
            firstName = firstName,
            lastName = lastName,
            password = password
        )

        val result = registerController.register(request)
        assertThat(result.statusCode.value()).isEqualTo(400)
        assertThat(result.body?.get("message")).isEqualTo(expectedMessage)
    }
}
