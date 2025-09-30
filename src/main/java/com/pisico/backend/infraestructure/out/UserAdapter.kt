package com.pisico.backend.infraestructure.out

import com.pisico.backend.application.exception.EmailAlreadyVerifiedException
import com.pisico.backend.application.exception.InvalidCredentialsException
import com.pisico.backend.application.exception.InvalidTokenException
import com.pisico.backend.application.exception.InvalidUserRegistrationException
import com.pisico.backend.application.ports.out.UserRepository
import com.pisico.backend.domain.entities.User
import com.pisico.backend.domain.entities.UserVerification
import com.pisico.backend.infraestructure.mapper.UserMapper
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
            .fetchOne() ?: throw InvalidTokenException("Invalid or expired verification token")

        if (userRecord.get(USERS.TOKEN_EXPIRY_DATE) == null || userRecord.get(USERS.TOKEN_EXPIRY_DATE).isBefore(LocalDateTime.now())) {
            throw InvalidTokenException("Token expired.")
        }

        if (userRecord.get(USERS.EMAIL_VERIFIED)) {
            throw EmailAlreadyVerifiedException("Email already verified")
        }

        try {
            val rowsAffected = dslContext.update(USERS)
                .set(USERS.EMAIL_VERIFIED, true)
                .set(USERS.VERIFICATION_TOKEN, null as String?)
                .set(USERS.TOKEN_EXPIRY_DATE, null as LocalDateTime?)
                .set(USERS.ROW_UPDATED_ON, LocalDateTime.now())
                .where(USERS.ID.eq(userRecord.get(USERS.ID)))
                .execute()

            if (rowsAffected == 0) {
                throw IllegalStateException("No user found with id ${userRecord.get(USERS.ID)}")
            }

            return UserVerification(
                email = userRecord.get(USERS.EMAIL),
                emailVerified = true
            )

        } catch (e: DataAccessException) {
            throw IllegalStateException("Failed to verify token for user ${userRecord.get(USERS.ID)}.", e)
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

    override fun updateVerificationToken(email: String, token: String, expiryDate: OffsetDateTime) {
        try {
            val rowsAffected = dslContext.update(USERS)
                .set(USERS.VERIFICATION_TOKEN, token)
                .set(USERS.TOKEN_EXPIRY_DATE, expiryDate.toLocalDateTime())
                .set(USERS.ROW_UPDATED_ON, LocalDateTime.now())
                .where(USERS.EMAIL.eq(email))
                .execute()

            if (rowsAffected == 0) {
                throw InvalidCredentialsException("No user found with email $email")
            }
        } catch (e: DataAccessException) {
            throw IllegalStateException("Failed to update verification token for $email.", e)
        }
    }
}
