package com.pisico.backend.infraestructure.mapper

import com.pisico.backend.domain.entities.User
import com.pisico.backend.infraestructure.out.dto.UserPersistenceDto
import com.pisico.backend.jooq.generated.tables.records.UsersRecord
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import java.time.OffsetDateTime

@Mapper(componentModel = "spring")
interface UserMapper {
    @Mapping(target = "passwordHash", source = "hashedPassword")
    fun toPersistenceDto(
        user: User,
        hashedPassword: String,
        verificationToken: String,
        tokenExpiryDate: OffsetDateTime,
        emailVerified: Boolean
    ): UserPersistenceDto
    
    fun toDomain(user : UsersRecord) : User?
}