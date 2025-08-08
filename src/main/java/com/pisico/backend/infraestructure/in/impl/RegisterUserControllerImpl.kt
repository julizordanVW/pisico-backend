package com.pisico.backend.infraestructure.`in`.impl

import com.pisico.backend.application.useCases.UserRegistrator
import com.pisico.backend.infraestructure.`in`.controller.auth.RegisterUserController
import com.pisico.backend.infraestructure.`in`.dto.user.registry.RegisterByEmailRequest
import com.pisico.backend.infraestructure.mapper.UserMapper
import org.springframework.web.bind.annotation.RestController

@RestController
class RegisterUserControllerImpl(
    private val userRegistrator: UserRegistrator,
    private val userMapper : UserMapper
) : RegisterUserController {

    override fun register(registerByEmailRequest: RegisterByEmailRequest) {
        userRegistrator.execute(registerByEmailRequest)
    }

}