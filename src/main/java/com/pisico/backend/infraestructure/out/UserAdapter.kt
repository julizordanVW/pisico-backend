package com.pisico.backend.infraestructure.out

import com.pisico.backend.application.exception.InvalidTokenException
import com.pisico.backend.application.exception.InvalidUserRegistrationException
import com.pisico.backend.application.exception.UserNotFoundException
import com.pisico.backend.application.ports.out.UserRepository
import com.pisico.backend.domain.entities.User
import com.pisico.backend.infraestructure.mapper.UserMapper
import com.pisico.backend.domain.entities.UserVerification
import com.pisico.backend.jooq.generated.Tables.USERS
import com.pisico.backend.jooq.generated.tables.records.UsersRecord
import org.jooq.DSLContext
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Repository
open class UserAdapter(
    private val dslContext: DSLContext,
    private val userMapper: UserMapper
) : UserRepository {

    override fun findByEmail(email: String): UsersRecord? {
        return dslContext.selectFrom(USERS)
            .where(USERS.EMAIL.eq(email))
            .fetchOne()
    }

    override fun verifyToken(token: String) : UserVerification {
        val userRecord = dslContext.selectFrom(USERS)
            .where(USERS.VERIFICATION_TOKEN.eq(token))
            .fetchOne() ?: throw UserNotFoundException("User not found")

        if (userRecord.tokenExpiryDate.isBefore(LocalDateTime.now())) {
            throw InvalidTokenException("Token expired")
        }

        if (userRecord.emailVerified) {
            throw InvalidTokenException("Email already verified")
        }

        try {
            val rowsAffected = dslContext.update(USERS)
                .set(USERS.EMAIL_VERIFIED, true)
                .set(USERS.VERIFICATION_TOKEN, null as String?)
                .set(USERS.TOKEN_EXPIRY_DATE, null as LocalDateTime?)
                .set(USERS.ROW_UPDATED_ON, LocalDateTime.now())
                .where(USERS.ID.eq(userRecord.id))
                .execute()

            if (rowsAffected == 0) {
                throw IllegalStateException("No user found with id ${userRecord.id}")
            }

            return UserVerification(
                email = userRecord.email,
                emailVerified = true
            )

        } catch (e: DataAccessException) {
            throw IllegalStateException("Failed to verify token for user ${userRecord.id}.", e)
        }
    }

    override fun save(
        user: User,
        hashedPassword: String,
        verificationToken: String,
        tokenExpiryDate: OffsetDateTime?
    ) {
        val persistenceDto = userMapper.toPersistenceDto(
            user = user,
            hashedPassword = hashedPassword,
            verificationToken = verificationToken,
            tokenExpiryDate = tokenExpiryDate,
        )

        try {
            dslContext.insertInto(USERS)
                .set(USERS.NAME, persistenceDto.name)
                .set(USERS.EMAIL, persistenceDto.email)
                .set(USERS.PASSWORD_HASH, persistenceDto.passwordHash)
                .set(USERS.EMAIL_VERIFIED, persistenceDto.emailVerified)
                .set(USERS.VERIFICATION_TOKEN, persistenceDto.verificationToken)
                .set(USERS.TOKEN_EXPIRY_DATE, persistenceDto.tokenExpiryDate?.toLocalDateTime())
                .set(USERS.GENDER, persistenceDto.gender)
                .set(USERS.ROW_CREATED_ON, LocalDateTime.now())
                .set(USERS.ROW_UPDATED_ON, LocalDateTime.now())
                .execute()
        } catch (_: DuplicateKeyException) {
            throw InvalidUserRegistrationException("An account with that email already exists.")
        } catch (e: DataAccessException) {
            throw IllegalStateException("Failed to save user with email ${persistenceDto.email}.", e)
        }
    }

    override fun updateUserByEmail(email: String) {
        try {
            dslContext.update(USERS)
                .set(USERS.ROW_UPDATED_ON, OffsetDateTime.now().toLocalDateTime())
                .where(USERS.EMAIL.eq(email))
                .execute()
        } catch (e: DataAccessException) {
            throw IllegalStateException("Failed to update user with email $email.", e)
        }
    }

    fun updateUser(user: User): User {
        try {
            val rowsAffected = dslContext.update(USERS)
                .set(USERS.NAME, user.name)
                .set(USERS.DESCRIPTION, user.description)
                .set(USERS.DATE_OF_BIRTH, user.dateOfBirth)
                .set(USERS.EMAIL, user.email)
                .set(USERS.PASSWORD_HASH, user.password)
                .set(USERS.EMAIL_VERIFIED, user.emailVerified)
                .set(USERS.VERIFICATION_TOKEN, user.verificationToken)
                .set(USERS.TOKEN_EXPIRY_DATE, user.tokenExpiryDate?.toLocalDateTime())
                .set(USERS.PHONE_NUMBER, user.phoneNumber)
                .set(USERS.PROFILE_PICTURE_URL, user.profilePictureUrl)
                .set(USERS.GENDER, user.gender.name)
                .set(USERS.ROLE, user.role)
                .set(USERS.ACCOUNT_STATUS, user.accountStatus)
                .set(USERS.TIME_ZONE, user.timeZone)
                .set(USERS.ROW_UPDATED_ON, LocalDateTime.now())
                .where(USERS.ID.eq(user.id))
                .execute()

            if (rowsAffected == 0) {
                throw IllegalStateException("No user found with id ${user.id}")
            }

            return user.copy()

        } catch (e: DataAccessException) {
            throw IllegalStateException("Failed to update user with id ${user.id}.", e)
        }
    }
}
