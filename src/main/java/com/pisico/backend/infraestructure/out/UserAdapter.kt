package com.pisico.backend.infraestructure.out

import com.pisico.backend.application.exception.EmailAlreadyVerifiedException
import com.pisico.backend.application.exception.ExpiredTokenException
import com.pisico.backend.application.exception.InvalidUserRegistrationException
import com.pisico.backend.application.exception.UserNotFoundException
import com.pisico.backend.application.ports.out.UserRepository
import com.pisico.backend.domain.entities.User
import com.pisico.backend.infraestructure.mapper.UserMapper
import com.pisico.backend.jooq.generated.Tables.USERS
import com.pisico.backend.jooq.generated.tables.records.UsersRecord
import org.jooq.DSLContext
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

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

    override fun verifyEmail(userId: UUID, token: String) {
        val userRecord = dslContext.selectFrom(USERS)
            .where(USERS.ID.eq(userId))
            .fetchOne()

        if (userRecord == null) throw UserNotFoundException("User with ID $userId not found for email verification.")

        if (userRecord.get(USERS.EMAIL_VERIFIED))
            throw EmailAlreadyVerifiedException("Email for user ID $userId is already verified. No action needed.")


        if (userRecord.get(USERS.VERIFICATION_TOKEN) != token)
            throw IllegalArgumentException("Invalid verification token for user ID $userId.")

        val expirationDate = userRecord.get(USERS.TOKEN_EXPIRY_DATE)

        if (expirationDate == null || expirationDate.isBefore(LocalDateTime.now())) {
            throw ExpiredTokenException("Verification token for user ID $userId has expired.")
        }

        dslContext.update(USERS)
            .set(USERS.EMAIL_VERIFIED, true)
            .set(USERS.VERIFICATION_TOKEN, null as String?)
            .set(USERS.TOKEN_EXPIRY_DATE, null as LocalDateTime?)
            .where(USERS.ID.eq(userId))
            .execute()
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

    override fun updateUser(email: String) {
        try {
            dslContext.update(USERS)
                .set(USERS.ROW_UPDATED_ON, OffsetDateTime.now().toLocalDateTime())
                .where(USERS.EMAIL.eq(email))
                .execute()
        } catch (e: DataAccessException) {
            throw IllegalStateException("Failed to update user with email $email.", e)
        }

    }
}
