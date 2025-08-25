package com.pisico.backend.infraestructure.mapper

import com.pisico.backend.domain.entities.User
import com.pisico.backend.infraestructure.`in`.dto.UserPersistenceDto
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
        tokenExpiryDate: OffsetDateTime
    ): UserPersistenceDto
}