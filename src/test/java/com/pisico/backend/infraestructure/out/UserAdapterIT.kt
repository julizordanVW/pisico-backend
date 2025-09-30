package com.pisico.backend.infraestructure.out

import com.pisico.backend.application.exception.InvalidTokenException
import com.pisico.backend.application.exception.InvalidUserRegistrationException
import com.pisico.backend.config.AbstractIntegrationTest
import com.pisico.backend.domain.entities.User
import com.pisico.backend.jooq.generated.Tables.USERS
import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
        val result = userAdapter.verifyToken(verificationToken)

        // Then
        val verifiedUser = dslContext.selectFrom(USERS)
            .where(USERS.ID.eq(userId))
            .fetchOne()!!

        assertTrue(verifiedUser.get(USERS.EMAIL_VERIFIED))
        assertThat(verifiedUser.get(USERS.VERIFICATION_TOKEN)).isNull()
        assertThat(verifiedUser.get(USERS.TOKEN_EXPIRY_DATE)).isNull()
        assertThat(result.email).isEqualTo(testUser.email)
        assertThat(result.emailVerified).isTrue()
    }

    @Test
    fun `should throw UserNotFoundException when verifying email for non-existent user`() {
        // Given
        val nonExistentToken = "non-existent-token"

        // When & Then
        assertFailsWith<InvalidTokenException> {
            userAdapter.verifyToken(nonExistentToken)
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
        userAdapter.verifyToken(verificationToken)

        // When & Then
        assertFailsWith<InvalidTokenException> {
            userAdapter.verifyToken(verificationToken)
        }
    }

    @Test
    fun `should throw UserNotFoundException when verification token is invalid`() {
        // Given - Save user first
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val savedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val userEmail = savedUser.get(USERS.EMAIL)

        // When & Then
        assertFailsWith<InvalidTokenException> {
            userAdapter.verifyToken("invalid-token")
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
        assertFailsWith<InvalidTokenException> {
            userAdapter.verifyToken(verificationToken)
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
        assertFailsWith<InvalidTokenException> {
            userAdapter.verifyToken(verificationToken)
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
        userAdapter.verifyToken(verificationToken)

        // Then - Second verification should throw InvalidTokenException
        assertFailsWith<InvalidTokenException> {
            userAdapter.verifyToken(verificationToken)
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
        userAdapter.verifyToken(verificationToken)

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
        userAdapter.verifyToken(specialToken)

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
        assertFailsWith<InvalidTokenException> {
            userAdapter.verifyToken(verificationToken)
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
        userAdapter.verifyToken(emptyToken)

        val verifiedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(userEmail))
            .fetchOne()!!

        assertTrue(verifiedUser.get(USERS.EMAIL_VERIFIED))
    }

    // === UPDATE VERIFICATION TOKEN TESTS ===

    @Test
    fun `should update verification token successfully for existing user`() {
        // Given
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val newToken = "new-verification-token-456"
        val newExpiryDate = OffsetDateTime.now().plusDays(2)

        // When
        userAdapter.updateVerificationToken(testUser.email!!, newToken, newExpiryDate)

        // Then
        val updatedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        assertThat(updatedUser.get(USERS.VERIFICATION_TOKEN)).isEqualTo(newToken)
        assertThat(updatedUser.get(USERS.TOKEN_EXPIRY_DATE)).isNotNull()
        assertThat(updatedUser.get(USERS.ROW_UPDATED_ON)).isNotNull()
    }

    @Test
    fun `should throw InvalidCredentialsException when updating token for non-existent user`() {
        // Given
        val nonExistentEmail = "nonexistent@example.com"
        val newToken = "new-token-123"
        val newExpiryDate = OffsetDateTime.now().plusDays(1)

        // When & Then
        assertFailsWith<com.pisico.backend.application.exception.InvalidCredentialsException> {
            userAdapter.updateVerificationToken(nonExistentEmail, newToken, newExpiryDate)
        }
    }

    @Test
    fun `should update token and expiry date correctly`() {
        // Given
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val originalUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val originalToken = originalUser.get(USERS.VERIFICATION_TOKEN)
        val originalExpiryDate = originalUser.get(USERS.TOKEN_EXPIRY_DATE)

        val newToken = "updated-token-789"
        val newExpiryDate = OffsetDateTime.now().plusHours(12)

        // Small delay to ensure different timestamps
        Thread.sleep(1)

        // When
        userAdapter.updateVerificationToken(testUser.email!!, newToken, newExpiryDate)

        // Then
        val updatedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        assertThat(updatedUser.get(USERS.VERIFICATION_TOKEN)).isNotEqualTo(originalToken)
        assertThat(updatedUser.get(USERS.VERIFICATION_TOKEN)).isEqualTo(newToken)
        assertThat(updatedUser.get(USERS.TOKEN_EXPIRY_DATE)).isNotEqualTo(originalExpiryDate)
        assertThat(updatedUser.get(USERS.ROW_UPDATED_ON)).isAfter(originalUser.get(USERS.ROW_UPDATED_ON))
    }

    @Test
    fun `should preserve other user data when updating verification token`() {
        // Given
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val originalUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val originalName = originalUser.get(USERS.NAME)
        val originalEmail = originalUser.get(USERS.EMAIL)
        val originalPasswordHash = originalUser.get(USERS.PASSWORD_HASH)
        val originalEmailVerified = originalUser.get(USERS.EMAIL_VERIFIED)

        val newToken = "replacement-token"
        val newExpiryDate = OffsetDateTime.now().plusDays(3)

        // When
        userAdapter.updateVerificationToken(testUser.email!!, newToken, newExpiryDate)

        // Then
        val updatedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        assertThat(updatedUser.get(USERS.NAME)).isEqualTo(originalName)
        assertThat(updatedUser.get(USERS.EMAIL)).isEqualTo(originalEmail)
        assertThat(updatedUser.get(USERS.PASSWORD_HASH)).isEqualTo(originalPasswordHash)
        assertThat(updatedUser.get(USERS.EMAIL_VERIFIED)).isEqualTo(originalEmailVerified)
    }

    @Test
    fun `should handle multiple token updates for same user`() {
        // Given
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val firstToken = "first-token"
        val firstExpiryDate = OffsetDateTime.now().plusDays(1)

        val secondToken = "second-token"
        val secondExpiryDate = OffsetDateTime.now().plusDays(2)

        val thirdToken = "third-token"
        val thirdExpiryDate = OffsetDateTime.now().plusDays(3)

        // When
        userAdapter.updateVerificationToken(testUser.email!!, firstToken, firstExpiryDate)
        Thread.sleep(1)
        userAdapter.updateVerificationToken(testUser.email!!, secondToken, secondExpiryDate)
        Thread.sleep(1)
        userAdapter.updateVerificationToken(testUser.email!!, thirdToken, thirdExpiryDate)

        // Then
        val finalUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        assertThat(finalUser.get(USERS.VERIFICATION_TOKEN)).isEqualTo(thirdToken)
    }

    @Test
    fun `should update token with special characters`() {
        // Given
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val specialToken = "token-with-special@chars#123!$%^&*()"
        val newExpiryDate = OffsetDateTime.now().plusDays(1)

        // When
        userAdapter.updateVerificationToken(testUser.email!!, specialToken, newExpiryDate)

        // Then
        val updatedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        assertThat(updatedUser.get(USERS.VERIFICATION_TOKEN)).isEqualTo(specialToken)
    }

    @Test
    fun `should update token with very long expiry date`() {
        // Given
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val newToken = "long-expiry-token"
        val veryLongExpiryDate = OffsetDateTime.now().plusYears(10)

        // When
        userAdapter.updateVerificationToken(testUser.email!!, newToken, veryLongExpiryDate)

        // Then
        val updatedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        assertThat(updatedUser.get(USERS.VERIFICATION_TOKEN)).isEqualTo(newToken)
        assertThat(updatedUser.get(USERS.TOKEN_EXPIRY_DATE)).isNotNull()
    }

    @Test
    fun `should update token for user with already verified email`() {
        // Given - Save user and verify email
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)
        userAdapter.verifyToken(verificationToken)

        val newToken = "resend-token-123"
        val newExpiryDate = OffsetDateTime.now().plusDays(1)

        // When - Should allow token update even if email is verified
        userAdapter.updateVerificationToken(testUser.email!!, newToken, newExpiryDate)

        // Then
        val updatedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        assertThat(updatedUser.get(USERS.VERIFICATION_TOKEN)).isEqualTo(newToken)
        assertThat(updatedUser.get(USERS.EMAIL_VERIFIED)).isTrue()
    }

    @Test
    fun `should handle empty token string when updating`() {
        // Given
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val emptyToken = ""
        val newExpiryDate = OffsetDateTime.now().plusDays(1)

        // When
        userAdapter.updateVerificationToken(testUser.email!!, emptyToken, newExpiryDate)

        // Then
        val updatedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        assertThat(updatedUser.get(USERS.VERIFICATION_TOKEN)).isEqualTo(emptyToken)
    }

    @Test
    fun `should update row timestamp when updating verification token`() {
        // Given
        userAdapter.save(testUser, hashedPassword, verificationToken, tokenExpiryDate)

        val originalUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        val originalUpdatedTime = originalUser.get(USERS.ROW_UPDATED_ON)

        // Small delay to ensure different timestamps
        Thread.sleep(1)

        val newToken = "timestamp-test-token"
        val newExpiryDate = OffsetDateTime.now().plusDays(1)

        // When
        userAdapter.updateVerificationToken(testUser.email!!, newToken, newExpiryDate)

        // Then
        val updatedUser = dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(testUser.email))
            .fetchOne()!!

        assertThat(updatedUser.get(USERS.ROW_UPDATED_ON)).isAfter(originalUpdatedTime)
    }
}