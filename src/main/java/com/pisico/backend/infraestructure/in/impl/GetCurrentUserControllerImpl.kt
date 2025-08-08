package com.pisico.backend.infraestructure.`in`.impl

import com.pisico.backend.application.useCases.UserRetriever
import com.pisico.backend.infraestructure.`in`.controller.user.GetCurrentUserController
import org.springframework.web.bind.annotation.RestController

@RestController
class GetCurrentUserControllerImpl(
    private val userRetriever: UserRetriever,
) : GetCurrentUserController {

    override fun getMe() {
        val response = userRetriever.execute()
        //val mappedContent = propertiesApiMapper.toResponseList(response)

//        return PageWrapper(
//            content = response,
//            hasNext = false,
//            pageNumber = 0
//        )
    }

}