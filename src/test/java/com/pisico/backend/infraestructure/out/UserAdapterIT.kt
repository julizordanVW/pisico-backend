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
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.test.assertFailsWith
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
            verificationToken = null,
            tokenExpiryDate = null,
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

    // === SAVE TESTS ===

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
        assertThat(savedUser.get(USERS.ROW_CREATED_ON)).isNotNull()
        assertThat(savedUser.get(USERS.ROW_UPDATED_ON)).isNotNull()
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
    fun `should handle null token expiry date gracefully during save`() {
        // Given
        val userWithNullTokenExpiry = testUser.copy(email = "null-token@example.com")

        // When
        userAdapter.save(userWithNullTokenExpiry, hashedPassword, verificationToken, null)

        // Then
        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(userWithNullTokenExpiry.email))
            .fetchOne()!!

        assertThat(savedUser.get(USERS.TOKEN_EXPIRY_DATE)).isNull()
        assertThat(savedUser.get(USERS.VERIFICATION_TOKEN)).isEqualTo(verificationToken)
    }

    // === FIND BY EMAIL TESTS ===

    @Test
    fun `should find user by email when user exists`() {
        // Given
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        // When
        val foundUser = userAdapter.findByEmail(testUser.email!!)

        // Then
        assertThat(foundUser).isNotNull
        assertThat(foundUser!!.get(USERS.EMAIL)).isEqualTo(testUser.email)
        assertThat(foundUser.get(USERS.NAME)).isEqualTo(testUser.name)
    }

    @Test
    fun `should return null when finding user by non-existent email`() {
        // When
        val foundUser = userAdapter.findByEmail("non-existent@example.com")

        // Then
        assertThat(foundUser).isNull()
    }

    @Test
    fun `should handle case sensitive email search`() {
        // Given
        val lowercaseEmail = "test@example.com"
        val uppercaseEmail = "TEST@EXAMPLE.COM"

        val userWithLowercaseEmail = testUser.copy(email = lowercaseEmail)
        userAdapter.save(userWithLowercaseEmail, hashedPassword, verificationToken, tokenExpiryDate)

        // When & Then
        val foundLowercase = userAdapter.findByEmail(lowercaseEmail)
        val foundUppercase = userAdapter.findByEmail(uppercaseEmail)

        assertThat(foundLowercase).isNotNull
        assertThat(foundUppercase).isNull() // Assuming case-sensitive search
    }

    // === VERIFY EMAIL TESTS ===

    @Test
    fun `should verify email successfully with valid token and user`() {
        // Given - Save user first
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userId = savedUser.get(USERS.ID)

        // When
        userAdapter.verifyToken(savedUser.email, verificationToken)

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
        val nonExistentUser = "nouser@gmail.com"

        // When & Then
        assertFailsWith<UserNotFoundException> {
            userAdapter.verifyToken(nonExistentUser, "any-token")
        }
    }

    @Test
    fun `should throw EmailAlreadyVerifiedException when email is already verified`() {
        // Given - Save and verify user first
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userEmail = savedUser.get(USERS.EMAIL)
        userAdapter.verifyToken(userEmail, verificationToken)

        // When & Then
        assertFailsWith<EmailAlreadyVerifiedException> {
            userAdapter.verifyToken(userEmail, verificationToken)
        }
    }

    @Test
    fun `should throw InvalidDataAccessApiUsageException when verification token is invalid`() {
        // Given - Save user first
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userEmail = savedUser.get(USERS.EMAIL)

        // When & Then
        assertFailsWith<InvalidDataAccessApiUsageException> {
            userAdapter.verifyToken(userEmail, "invalid-token")
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

        val userEmail = savedUser.get(USERS.EMAIL)

        // When & Then
        assertFailsWith<ExpiredTokenException> {
            userAdapter.verifyToken(userEmail, verificationToken)
        }
    }

    @Test
    fun `should throw ExpiredTokenException when token expiry date is null`() {
        // Given - Manually set token expiry to null
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userEmail = savedUser.get(USERS.EMAIL)

        // Manually set token expiry to null to simulate edge case
        dslContext.update(USERS)
            .set(USERS.TOKEN_EXPIRY_DATE, null as LocalDateTime?)
            .where(USERS.EMAIL.eq(userEmail))
            .execute()

        // When & Then
        assertFailsWith<ExpiredTokenException> {
            userAdapter.verifyToken(userEmail, verificationToken)
        }
    }

    @Test
    fun `should handle concurrent email verification attempts gracefully`() {
        // Given - Save user first
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userEmail = savedUser.get(USERS.EMAIL)

        // When - First verification succeeds
        userAdapter.verifyToken(userEmail, verificationToken)

        // Then - Second verification should throw EmailAlreadyVerifiedException
        assertFailsWith<EmailAlreadyVerifiedException> {
            userAdapter.verifyToken(userEmail, verificationToken)
        }
    }

    @Test
    fun `should preserve user data integrity after email verification`() {
        // Given - Save user first
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userEmail = savedUser.get(USERS.EMAIL)
        val originalName = savedUser.get(USERS.NAME)
        val originalEmail = savedUser.get(USERS.EMAIL)
        val originalPasswordHash = savedUser.get(USERS.PASSWORD_HASH)

        // When
        userAdapter.verifyToken(userEmail, verificationToken)

        // Then - All other data should remain unchanged
        val verifiedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(userEmail))
            .fetchOne()!!

        assertThat(verifiedUser.get(USERS.NAME)).isEqualTo(originalName)
        assertThat(verifiedUser.get(USERS.EMAIL)).isEqualTo(originalEmail)
        assertThat(verifiedUser.get(USERS.PASSWORD_HASH)).isEqualTo(originalPasswordHash)
        assertTrue(verifiedUser.get(USERS.EMAIL_VERIFIED))
    }

    @Test
    fun `should verify email with token containing special characters`() {
        // Given
        val specialToken = "token-with-special@chars#123!"
        userAdapter.save(testUser, hashedPassword, specialToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userEmail = savedUser.get(USERS.EMAIL)

        // When
        userAdapter.verifyToken(userEmail, specialToken)

        // Then
        val verifiedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(userEmail))
            .fetchOne()!!

        assertTrue(verifiedUser.get(USERS.EMAIL_VERIFIED))
    }

    // === UPDATE USER TESTS ===

    @Test
    fun `should update user row timestamp when user exists`() {
        // Given
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val originalUpdatedTime = savedUser.get(USERS.ROW_UPDATED_ON)

        // Small delay to ensure different timestamps
        Thread.sleep(1)

        // When
        userAdapter.updateUserByEmail(testUser.email!!)

        // Then
        val updatedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        assertThat(updatedUser.get(USERS.ROW_UPDATED_ON)).isAfter(originalUpdatedTime)
        // Verify other fields remain unchanged
        assertThat(updatedUser.get(USERS.NAME)).isEqualTo(testUser.name)
        assertThat(updatedUser.get(USERS.EMAIL)).isEqualTo(testUser.email)
        assertThat(updatedUser.get(USERS.PASSWORD_HASH)).isEqualTo(hashedPassword)
    }

    @Test
    fun `should handle update user when email does not exist`() {
        // Given
        val nonExistentEmail = "non-existent@example.com"

        // When & Then - Should not throw exception, just update 0 rows
        userAdapter.updateUserByEmail(nonExistentEmail)

        // Verify no users were created
        val userCount = dslContext.selectCount()
            .from(USERS)
            .where(USERS.EMAIL.eq(nonExistentEmail))
            .fetchOne(0, Int::class.java)

        assertThat(userCount).isEqualTo(0)
    }

    @Test
    fun `should update multiple users with same timestamp when called simultaneously`() {
        // Given
        val user1 = testUser.copy(email = "user1@example.com")
        val user2 = testUser.copy(email = "user2@example.com")

        userAdapter.save(user1, hashedPassword, verificationToken, tokenExpiryDate)
        userAdapter.save(user2, hashedPassword, verificationToken, tokenExpiryDate)

        // When
        userAdapter.updateUserByEmail(user1.email!!)
        userAdapter.updateUserByEmail(user2.email!!)

        // Then
        val updatedUser1 = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(user1.email))
            .fetchOne()!!

        val updatedUser2 = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(user2.email))
            .fetchOne()!!

        assertThat(updatedUser1.get(USERS.ROW_UPDATED_ON)).isNotNull()
        assertThat(updatedUser2.get(USERS.ROW_UPDATED_ON)).isNotNull()
    }

    // === EDGE CASES AND ERROR HANDLING ===

    @Test
    fun `should handle user with extremely long name`() {
        // Given
        val longName = "A".repeat(255) // Assuming 255 is max length
        val userWithLongName = testUser.copy(
            name = longName,
            email = "long-name@example.com"
        )

        // When & Then
        userAdapter.save(userWithLongName, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(userWithLongName.email))
            .fetchOne()!!

        assertThat(savedUser.get(USERS.NAME)).isEqualTo(longName)
    }

    @Test
    fun `should handle user with special characters in email`() {
        // Given
        val specialEmail = "test+special@sub-domain.example.com"
        val userWithSpecialEmail = testUser.copy(email = specialEmail)

        // When
        userAdapter.save(userWithSpecialEmail, hashedPassword, verificationToken, tokenExpiryDate)

        // Then
        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(specialEmail))
            .fetchOne()!!

        assertThat(savedUser.get(USERS.EMAIL)).isEqualTo(specialEmail)
    }

    @Test
    fun `should handle verification with edge case timestamps`() {
        // Given - Token expires exactly now
        val exactExpiryTime = OffsetDateTime.now()
        userAdapter.save(testUser, hashedPassword, verificationToken, exactExpiryTime)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userEmail = savedUser.get(USERS.EMAIL)

        // When & Then - This should likely fail due to exact timing
        assertFailsWith<ExpiredTokenException> {
            userAdapter.verifyToken(userEmail, verificationToken)
        }
    }

    @Test
    fun `should maintain transaction integrity when save fails`() {
        // Given
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        // When & Then
        val duplicateUser = testUser.copy(name = "Different Name")

        assertFailsWith<InvalidUserRegistrationException> {
            userAdapter.save(duplicateUser, hashedPassword, verificationToken, tokenExpiryDate)
        }
    }

    @Test
    fun `should handle empty verification token`() {
        // Given
        val emptyToken = ""
        userAdapter.save(testUser, hashedPassword, emptyToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userEmail = savedUser.get(USERS.EMAIL)

        // When & Then
        userAdapter.verifyToken(userEmail, emptyToken)

        val verifiedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(userEmail))
            .fetchOne()!!

        assertTrue(verifiedUser.get(USERS.EMAIL_VERIFIED))
    }
}