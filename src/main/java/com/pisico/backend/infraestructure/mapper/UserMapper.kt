package com.pisico.backend.infraestructure.mapper

import com.pisico.backend.domain.entities.User
import com.pisico.backend.infraestructure.out.dto.UserPersistenceDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import java.time.OffsetDateTime

@Mapper(componentModel = "spring")
interface UserMapper {
    @Mapping(target = "passwordHash", source = "hashedPassword")
    @Mapping(target = "verificationToken", source = "verificationToken")
    @Mapping(target = "tokenExpiryDate", source = "tokenExpiryDate")
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "rowCreatedOn", ignore = true)
    @Mapping(target = "rowUpdatedOn", ignore = true)
    fun toPersistenceDto(
        user: User,
        hashedPassword: String,
        verificationToken: String,
        tokenExpiryDate: OffsetDateTime?,
    ): UserPersistenceDto
}