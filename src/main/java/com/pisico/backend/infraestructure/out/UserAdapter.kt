package com.pisico.backend.infraestructure.out

import com.pisico.backend.application.exception.EmailAlreadyVerifiedException
import com.pisico.backend.application.exception.ExpiredTokenException
import com.pisico.backend.application.exception.UserNotFoundException
import com.pisico.backend.application.ports.out.UsersRepository
import com.pisico.backend.domain.entities.User
import com.pisico.backend.infraestructure.mapper.UserMapper
import com.pisico.backend.jooq.generated.Tables.USERS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

@Repository
open class UserAdapter(
    private val dslContext: DSLContext,
    private val userMapper: UserMapper
) : UsersRepository {

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
        tokenExpiryDate: OffsetDateTime
    ) {
        val persistenceDto = userMapper.toPersistenceDto(
            user = user,
            hashedPassword = hashedPassword,
            verificationToken = verificationToken,
            tokenExpiryDate = tokenExpiryDate
        )

        dslContext.insertInto(USERS)
            .set(USERS.NAME, persistenceDto.name)
            .set(USERS.EMAIL, persistenceDto.email)
            .set(USERS.PASSWORD_HASH, persistenceDto.passwordHash)
            .set(USERS.EMAIL_VERIFIED, persistenceDto.emailVerified)
            .set(USERS.VERIFICATION_TOKEN, persistenceDto.verificationToken)
            .set(USERS.TOKEN_EXPIRY_DATE, persistenceDto.tokenExpiryDate?.toLocalDateTime())
            .set(USERS.GENDER, persistenceDto.gender)
            .execute()
    }
}
