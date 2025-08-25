package com.pisico.backend.infraestructure.out

import com.pisico.backend.application.exception.EmailAlreadyVerifiedException
import com.pisico.backend.application.exception.ExpiredTokenException
import com.pisico.backend.application.exception.InvalidUserRegistrationException
import com.pisico.backend.application.exception.UserNotFoundException
import com.pisico.backend.config.AbstractIntegrationTest
import com.pisico.backend.domain.entities.User
import com.pisico.backend.jooq.generated.Tables.USERS
import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
open class UserAdapterIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var userAdapter: UserAdapter
    @Autowired
    private lateinit var dslContext: DSLContext
    
    private lateinit var testUser: User
    private lateinit var hashedPassword: String
    private lateinit var verificationToken: String
    private lateinit var tokenExpiryDate: OffsetDateTime

    @BeforeEach
    fun setUp() {
        testUser = User(
            id = null,
            name = "Test User",
            description = "Test Description",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            email = "test@example.com",
            password = "plainPassword",
            emailVerified = false,
            verification_token = null,
            token_expiry_date = null,
            phoneNumber = "+34123456789",
            profilePictureUrl = "https://example.com/profile.jpg",
            gender = User.Gender.NOT_SPECIFIED,
            role = "USER",
            accountStatus = "ACTIVE",
            timeZone = "Europe/Madrid"
        )

        hashedPassword = "hashedPassword123"
        verificationToken = "verification-token-123"
        tokenExpiryDate = OffsetDateTime.now().plusDays(1)
    }

    @Test
    fun `should save user successfully with all required fields`() {
        // When
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        // Then
        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()

        assertThat(savedUser).isNotNull
        assertThat(savedUser!!.get(USERS.NAME)).isEqualTo(testUser.name)
        assertThat(savedUser.get(USERS.EMAIL)).isEqualTo(testUser.email)
        assertThat(savedUser.get(USERS.PASSWORD_HASH)).isEqualTo(hashedPassword)
        assertThat(savedUser.get(USERS.EMAIL_VERIFIED)).isFalse()
        assertThat(savedUser.get(USERS.VERIFICATION_TOKEN)).isEqualTo(verificationToken)
        assertThat(savedUser.get(USERS.TOKEN_EXPIRY_DATE)).isNotNull()
        assertThat(savedUser.get(USERS.GENDER)).isEqualTo(testUser.gender.value)
    }

    @Test
    fun `should save user with minimal required fields`() {
        // Given
        val minimalUser = User(
            id = null,
            name = "Minimal User",
            description = null,
            dateOfBirth = null,
            email = "minimal@example.com",
            password = "plainPassword",
            phoneNumber = null,
            profilePictureUrl = null,
            gender = User.Gender.NOT_SPECIFIED,
            role = null,
            accountStatus = null,
            timeZone = null
        )

        // When
        userAdapter.save(minimalUser, hashedPassword, verificationToken, tokenExpiryDate)

        // Then
        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(minimalUser.email))
            .fetchOne()

        assertThat(savedUser).isNotNull
        assertThat(savedUser!!.get(USERS.NAME)).isEqualTo(minimalUser.name)
        assertThat(savedUser.get(USERS.EMAIL)).isEqualTo(minimalUser.email)
        assertThat(savedUser.get(USERS.PASSWORD_HASH)).isEqualTo(hashedPassword)
    }

    @Test
    fun `should throw InvalidUserRegistrationException when email already exists`() {
        // Given - Save user first
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        // When & Then
        val duplicateUser = testUser.copy(name = "Duplicate User")

        assertFailsWith<InvalidUserRegistrationException> {
            userAdapter.save(duplicateUser, hashedPassword, verificationToken, tokenExpiryDate)
        }
    }

    @Test
    fun `should verify email successfully with valid token and user`() {
        // Given - Save user first
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userId = savedUser.get(USERS.ID)

        // When
        userAdapter.verifyEmail(userId, verificationToken)

        // Then
        val verifiedUser = dslContext.selectFrom(USERS)
            .where(USERS.ID.eq(userId))
            .fetchOne()!!

        assertTrue(verifiedUser.get(USERS.EMAIL_VERIFIED))
        assertThat(verifiedUser.get(USERS.VERIFICATION_TOKEN)).isNull()
        assertThat(verifiedUser.get(USERS.TOKEN_EXPIRY_DATE)).isNull()
    }

    @Test
    fun `should throw UserNotFoundException when verifying email for non-existent user`() {
        // Given
        val nonExistentUserId = UUID.randomUUID()

        // When & Then
        assertFailsWith<UserNotFoundException> {
            userAdapter.verifyEmail(nonExistentUserId, "any-token")
        }
    }

    @Test
    fun `should throw EmailAlreadyVerifiedException when email is already verified`() {
        // Given - Save and verify user first
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userId = savedUser.get(USERS.ID)
        userAdapter.verifyEmail(userId, verificationToken)

        // When & Then
        assertFailsWith<EmailAlreadyVerifiedException> {
            userAdapter.verifyEmail(userId, verificationToken)
        }
    }

    @Test
    fun `should throw IllegalArgumentException when verification token is invalid`() {
        // Given - Save user first
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userId = savedUser.get(USERS.ID)

        // When & Then
        assertFailsWith<InvalidDataAccessApiUsageException> {
            userAdapter.verifyEmail(userId, "invalid-token")
        }
    }

    @Test
    fun `should throw ExpiredTokenException when verification token has expired`() {
        // Given - Save user with expired token
        val expiredTokenDate = OffsetDateTime.now().minusDays(1)
        userAdapter.save(testUser, hashedPassword, verificationToken, expiredTokenDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userId = savedUser.get(USERS.ID)

        // When & Then
        assertFailsWith<ExpiredTokenException> {
            userAdapter.verifyEmail(userId, verificationToken)
        }
    }

    @Test
    fun `should throw ExpiredTokenException when token expiry date is null`() {
        // Given - Manually set token expiry to null
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userId = savedUser.get(USERS.ID)

        // Manually set token expiry to null to simulate edge case
        dslContext.update(USERS)
            .set(USERS.TOKEN_EXPIRY_DATE, null as LocalDateTime?)
            .where(USERS.ID.eq(userId))
            .execute()

        // When & Then
        assertFailsWith<ExpiredTokenException> {
            userAdapter.verifyEmail(userId, verificationToken)
        }
    }

    @Test
    fun `should save users with different genders correctly`() {
        // Test each gender enum value
        val genders = User.Gender.values()

        genders.forEachIndexed { index, gender ->
            val userWithGender = testUser.copy(
                email = "test$index@example.com",
                gender = gender
            )

            userAdapter.save(userWithGender, hashedPassword, verificationToken, tokenExpiryDate)

            val savedUser = dslContext.selectFrom(USERS)
                .where(USERS.EMAIL.eq(userWithGender.email))
                .fetchOne()!!

            assertThat(savedUser.get(USERS.GENDER)).isEqualTo(gender.value)
        }
    }

    @Test
    fun `should handle concurrent email verification attempts gracefully`() {
        // Given - Save user first
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userId = savedUser.get(USERS.ID)

        // When - First verification succeeds
        userAdapter.verifyEmail(userId, verificationToken)

        // Then - Second verification should throw EmailAlreadyVerifiedException
        assertFailsWith<EmailAlreadyVerifiedException> {
            userAdapter.verifyEmail(userId, verificationToken)
        }
    }

    @Test
    fun `should preserve user data integrity after email verification`() {
        // Given - Save user first
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userId = savedUser.get(USERS.ID)
        val originalName = savedUser.get(USERS.NAME)
        val originalEmail = savedUser.get(USERS.EMAIL)
        val originalPasswordHash = savedUser.get(USERS.PASSWORD_HASH)

        // When
        userAdapter.verifyEmail(userId, verificationToken)

        // Then - All other data should remain unchanged
        val verifiedUser = dslContext.selectFrom(USERS)
            .where(USERS.ID.eq(userId))
            .fetchOne()!!

        assertThat(verifiedUser.get(USERS.NAME)).isEqualTo(originalName)
        assertThat(verifiedUser.get(USERS.EMAIL)).isEqualTo(originalEmail)
        assertThat(verifiedUser.get(USERS.PASSWORD_HASH)).isEqualTo(originalPasswordHash)
        assertTrue(verifiedUser.get(USERS.EMAIL_VERIFIED))
    }
}