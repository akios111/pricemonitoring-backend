package iee.ihu.gr.pricemonitoringbackend.controller

import iee.ihu.gr.pricemonitoringbackend.MONITOR_LISTS_UNIQUE_NAME_PER_OWNER
import iee.ihu.gr.pricemonitoringbackend.dto.MonitorListCreateForm
import iee.ihu.gr.pricemonitoringbackend.dto.MonitorListInfo
import iee.ihu.gr.pricemonitoringbackend.dto.MonitorListUpdateForm
import iee.ihu.gr.pricemonitoringbackend.dto.RoomInfo
import iee.ihu.gr.pricemonitoringbackend.entity.User
import iee.ihu.gr.pricemonitoringbackend.entity.Email
import iee.ihu.gr.pricemonitoringbackend.entity.MonitorList
import iee.ihu.gr.pricemonitoringbackend.entity.Role
import io.mockk.*
import kotlinx.serialization.encodeToString
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class MonitorListControllerUnitTest : AbstractControllerUnitTest() {

    private val createForm = MonitorListCreateForm(setOf(1,2),"any_name",setOf(1,2,3))

    private val updateForm = MonitorListUpdateForm(setOf(1,2),"new_name",setOf(1,2,3),1)

    @Test
    fun `should return 401(Unauthorized) response when trying to create monitor list without being authenticated`() {
        mockMvc
            .post("/monitor_list")
            .andExpect { status { isUnauthorized() } }
        verifyAll { monitorListServiceMock wasNot called }
    }

    @Test
    fun `should return 401(Unauthorized) response when trying to get monitor lists without being authenticated`() {
        mockMvc
            .get("/monitor_list")
            .andExpect { status { isUnauthorized() } }
        verifyAll { monitorListServiceMock wasNot called }
    }

    @Test
    fun `should return 401(Unauthorized) response when trying to get monitor list details without being authenticated`() {
        mockMvc
            .get("/monitor_list/1")
            .andExpect { status { isUnauthorized() } }
        verifyAll { monitorListServiceMock wasNot called }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return conflict response when trying to create a monitor list with name that is already used by another monitor list of the same user`(role: Role) {
        every { monitorListServiceMock.createMonitorList(createForm) } throws DataIntegrityViolationException(MONITOR_LISTS_UNIQUE_NAME_PER_OWNER)

        mockMvc
            .post("/monitor_list"){
                with(user("1").roles(role.name))
                contentType = MediaType.APPLICATION_JSON
                content = json.encodeToString(createForm)
            }.andExpect {
                status { isConflict() }
                content { string(messageSource.getMessage("Monitor.List.Unique.Name.Per.Owner.message",arrayOf(createForm.name),LocaleContextHolder.getLocale())) }
            }
        verifyAll { monitorListServiceMock.createMonitorList(createForm) }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return 200(OK) response on successful monitor list creation`(role: Role) {
        every { monitorListServiceMock.createMonitorList(createForm) } returns mockk()

        mockMvc
            .post("/monitor_list"){
                with(user("1").roles(role.name))
                contentType = MediaType.APPLICATION_JSON
                content = json.encodeToString(createForm)
            }.andExpect {
                status { isOk() }
            }

        verifyAll { monitorListServiceMock.createMonitorList(createForm) }
    }


    @MethodSource("allExceptPostAndGetMethodsWithRoles")
    @ParameterizedTest
    fun `should return 401(Unauthorized) response when trying to request monitor list creation or to get monitor lists with invalid http method`(parameters: Pair<HttpMethod,Role>) {
        val (invalidHttpMethod,role) = parameters
        mockMvc
            .perform {
                request(invalidHttpMethod,"/monitor_list")
                    .with(user("1").roles(role.name))
                    .contentType(MediaType.APPLICATION_JSON)
                    .buildRequest(it)
            }.andExpect(status().isUnauthorized)

        verifyAll { monitorListServiceMock wasNot called }
    }

    @MethodSource("allExceptGetMethodsWithRoles")
    @ParameterizedTest
    fun `should return 401(Unauthorized) response when trying to request monitor list details with invalid http method`(parameters: Pair<HttpMethod, Role>) {
        val (invalidHttpMethod,role) = parameters
        mockMvc
            .perform{
                request(invalidHttpMethod,"/monitor_list/1")
                    .with(user("1").roles(role.name))
                    .contentType(MediaType.APPLICATION_JSON)
                    .buildRequest(it)
            }.andExpect(status().isUnauthorized)

        verifyAll { monitorListServiceMock wasNot called }
    }

    @MethodSource("mediaTypesWithoutJsonWithRoles")
    @ParameterizedTest
    fun `should return 415(Unsupported Media Type) when trying to create monitor list with invalid content type`(parameters: Pair<String, Role>) {
        val (invalidContentType,role) = parameters
        mockMvc
            .post("/monitor_list"){
                with(user("1").roles(role.name))
                contentType = MediaType.parseMediaType(invalidContentType)
            }.andExpect {
                status { isUnsupportedMediaType() }
            }

        verifyAll { monitorListServiceMock wasNot called }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return 200(OK) response with user's monitor lists`(role: Role) {
        val returnedMonitorLists = listOf(MonitorList(mutableSetOf(), "ANY_NAME_1", mockk(), id = 1),MonitorList(
            mutableSetOf(),
            "ANY_NAME_2",
            mockk(),
            id = 4
        ))
        every { monitorListServiceMock.monitorLists(1) } returns returnedMonitorLists

        mockMvc
            .get("/monitor_list"){ with(user("1").roles(role.name)) }
            .andExpect {
                status { isOk() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(json.encodeToString(returnedMonitorLists.map { MonitorListInfo(it.name,it.id!!) }))
                }
            }

        verifyAll {
            monitorListServiceMock.monitorLists(1)
        }
    }

    @MethodSource("mediaTypesWithoutJsonWithRoles")
    @ParameterizedTest
    fun `should return 406(Not Acceptable) response when trying to request user monitor lists with unacceptable accept content type`(parameters: Pair<String, Role>) {
        val (invalidAcceptContentType,role) = parameters
        mockMvc
            .get("/monitor_list"){
                with(user("1").roles(role.name))
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.parseMediaType(invalidAcceptContentType)
            }.andExpect {
                status { isNotAcceptable() }
            }

        verifyAll { monitorListServiceMock wasNot called }
    }

    @MethodSource("mediaTypesWithoutJsonWithRoles")
    @ParameterizedTest
    fun `should return 406(Not Acceptable) response when trying to request user monitor list with unacceptable accept content type`(parameters: Pair<String, Role>) {
        val (invalidAcceptContentType,role) = parameters
        mockMvc
            .get("/monitor_list/1"){
                with(user("1").roles(role.name))
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.parseMediaType(invalidAcceptContentType)
            }.andExpect {
                status { isNotAcceptable() }
            }

        verifyAll { monitorListServiceMock wasNot called }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return 403(Forbidden) response status when trying to request monitor list details and owner is different from requester`(role: Role) {
        val monitorListID = 1L
        every { monitorListServiceMock.findMonitorList(monitorListID) } returns MonitorList(
            mutableSetOf(),
            "ANY_NAME",
            User(Email("ANY_EMAIL"),"ANY_NAME","ANY_PASS").apply { id=2 }) // requester's id is 1

        mockMvc
            .get("/monitor_list/$monitorListID"){
                with(user("1").roles(role.name))
            }
            .andExpect { status { isForbidden() } }

        verifyAll { monitorListServiceMock.findMonitorList(monitorListID) }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return 200(OK) response with monitor list details`(role: Role) {
        val monitorListID = 1L
        val monitorList = MonitorList(mutableSetOf(), "ANY_NAME", User(Email("ANY_EMAIL"),"ANY_NAME","ANY_PASS").apply { id=1 })
        every { statisticServiceMock.roomStatistics(monitorList,null,null,null) } returns emptyMap()
        every {
            monitorListServiceMock.findMonitorList(monitorListID)
        } returns monitorList

        mockMvc
            .get("/monitor_list/$monitorListID"){
                with(user("1").roles(role.name))
            }.andExpect {
                status { isOk() }
                content {
                    jsonPath("$.monitorListName",monitorList.name)
                    jsonPath("$.monitorListID",monitorList.id)
                    jsonPath("$.rooms", Matchers.hasSize<RoomInfo>(0))
                }
            }

        verifyAll { monitorListServiceMock.findMonitorList(monitorListID) }
    }

    @WithAnonymousUser
    @Test
    fun `should return 401(Unauthorized) response when trying to delete monitor list without being authenticated`() {
        mockMvc
            .delete("/monitor_list/1")
            .andExpect { status { isUnauthorized() } }

        verifyAll { monitorListServiceMock wasNot called }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return 200(OK) response on successful monitor list deletion`(role: Role) {
        every { monitorListServiceMock.delete(1) } just runs

        mockMvc
            .delete("/monitor_list/1"){
                with(user("1").roles(role.name))
            }.andExpect { status { isOk() } }

        verifyAll { monitorListServiceMock.delete(1) }
    }

    @WithAnonymousUser
    @Test
    fun `should return 401(Unauthorized) response when trying to update monitor list without being authenticated`() {
        mockMvc
            .put("/monitor_list"){
                content = json.encodeToString(updateForm)
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isUnauthorized() }
            }

        verifyAll { monitorListServiceMock wasNot called }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return 409(Conflict) when updating monitor list causes name conflict`(role: Role) {
        every { monitorListServiceMock.update(updateForm) } throws DataIntegrityViolationException(MONITOR_LISTS_UNIQUE_NAME_PER_OWNER)

        mockMvc
            .put("/monitor_list"){
                with(user("1").roles(role.name))
                content = json.encodeToString(updateForm)
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isConflict() }
            }

        verifyAll { monitorListServiceMock.update(updateForm) }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should update monitor list and return 200(OK) response`(role: Role) {
        every { monitorListServiceMock.update(updateForm) } just runs

        mockMvc
            .put("/monitor_list"){
                with(user("1").roles(role.name))
                content = json.encodeToString(updateForm)
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
            }

        verifyAll { monitorListServiceMock.update(updateForm) }
    }


    @MethodSource("mediaTypesWithoutJsonWithRoles")
    @ParameterizedTest
    fun `should return 415(Unsupported media type) when trying to update monitor list with invalid content type`(parameters: Pair<String, Role>) {
        val (invalidContentType,role) = parameters
        mockMvc
            .put("/monitor_list"){
                with(user("1").roles(role.name))
                content = json.encodeToString(updateForm)
                contentType = MediaType.parseMediaType(invalidContentType)
            }.andExpect {
                status { isUnsupportedMediaType() }
            }

        verifyAll { monitorListServiceMock wasNot called }
    }
}