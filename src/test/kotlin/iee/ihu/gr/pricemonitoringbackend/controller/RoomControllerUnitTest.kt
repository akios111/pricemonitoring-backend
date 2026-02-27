package iee.ihu.gr.pricemonitoringbackend.controller

import iee.ihu.gr.pricemonitoringbackend.dto.RoomChange
import iee.ihu.gr.pricemonitoringbackend.entity.Role
import io.mockk.called
import io.mockk.every
import io.mockk.verifyAll
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class RoomControllerUnitTest : AbstractControllerUnitTest() {


    @WithAnonymousUser
    @Test
    fun `should return 401(Unauthorized) when trying to access room's history without being authenticated`() {
        mockMvc
            .get("/room/history/1")
            .andExpect { status { isUnauthorized() } }

        verifyAll { roomServiceMock wasNot called }
    }


    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return 200(OK) response with response containing the first page of room's history when requesting without specifying page number and size`(role: Role) {
        val defaultPageable = PageRequest.of(0,20)
        val page = PageImpl(listOf(RoomChange(listOf("ADDED_ATTR_1"),listOf("REMOVED_ATTR_1"),1)),defaultPageable,3)
        every { roomServiceMock.changes(1,defaultPageable) } returns page

        mockMvc
            .get("/room/history/1"){
                with(user("1").roles(role.name))
            }
            .andExpect {
                status { isOk() }
                content {
                    jsonPath("$._embedded.roomChangeList",Matchers.hasSize<Any>(1))
                    jsonPath("$._embedded.roomChangeList[0].addedAttributes",Matchers.hasSize<Any>(1))
                    jsonPath("$._embedded.roomChangeList[0].removedAttributes",Matchers.hasSize<Any>(1))
                    jsonPath("$._embedded.roomChangeList[0].addedAttributes[0]",Matchers.`is`(page.content[0].addedAttributes[0]))
                    jsonPath("$._embedded.roomChangeList[0].removedAttributes[0]",Matchers.`is`(page.content[0].removedAttributes[0]))
                    jsonPath("$._embedded.roomChangeList[0].timestamp",Matchers.`is`(page.content[0].timestamp.toInt()))
                }
            }

        verifyAll { roomServiceMock.changes(1,defaultPageable) }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return 200(OK) response when requesting room's history with specific page number and size`(role: Role) {
        val expectedPage = PageRequest.of(4,10)
        val page = PageImpl(listOf(RoomChange(listOf("ADDED_ATTR_1"),listOf("REMOVED_ATTR_1"),1)),expectedPage,3)
        every { roomServiceMock.changes(1,expectedPage) } returns page

        mockMvc
            .get("/room/history/1?page=${expectedPage.pageNumber}&size=${expectedPage.pageSize}"){
                with(user("1").roles(role.name))
            }.andExpect { status { isOk() } }

        verifyAll { roomServiceMock.changes(1,expectedPage) }
    }

    @MethodSource("mediaTypesWithoutJsonWithRoles")
    @ParameterizedTest
    fun `should return 406(Not Acceptable) response when trying to get room's history with invalid accept header`(parameters: Pair<String, Role>) {
        val (invalidContentType,role) = parameters
        mockMvc.get("/room/history/1"){
            with(user("1").roles(role.name))
            accept = MediaType.parseMediaType(invalidContentType)
        }.andExpect {
            status { isNotAcceptable() }
        }

        verifyAll { roomServiceMock wasNot called }
    }

    @MethodSource("allExceptGetMethodsWithRoles")
    @ParameterizedTest
    fun `should return 401(Unauthorized) when trying to use http method other than GET to request room's history`(parameters: Pair<HttpMethod,Role>) {
        val (invalidHttpMethod,role) = parameters
        mockMvc
            .perform {
                request(invalidHttpMethod,"/room/history/1")
                    .with(user("1").roles(role.name))
                    .buildRequest(it)
            }
            .andExpect(status().isUnauthorized)

        verifyAll { roomServiceMock wasNot called }
    }
}