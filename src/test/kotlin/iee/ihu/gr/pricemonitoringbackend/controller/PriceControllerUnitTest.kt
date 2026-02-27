package iee.ihu.gr.pricemonitoringbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import iee.ihu.gr.pricemonitoringbackend.dto.PriceInfo
import iee.ihu.gr.pricemonitoringbackend.entity.*
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import kotlinx.serialization.encodeToString
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.get
import java.time.LocalDateTime
import kotlin.math.exp

class PriceControllerUnitTest : AbstractControllerUnitTest() {

    @Test
    fun `should return 401(Unauthorized) when trying to request room's prices without being authenticated`() {
        mockMvc
            .get("/prices/2/1")
            .andExpect { status { isUnauthorized() } }

        verifyAll { priceServiceMock wasNot called }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return 200(OK) response with room's prices`(role: Role) {
        every { monitorListServiceMock.isOwner(2,1) } returns true
        val roomView = RoomView(LocalDateTime.now(),4, mockk(),CancellationPolicy.FREE_CANCELLATION,"free breakfast")
        val roomViewPrices = listOf(
            RoomViewPrice(1,10,5,4,roomView, LocalDateTime.now(),1),
            RoomViewPrice(2,20,3,0,roomView, LocalDateTime.now(),2)
        )
        val expectedPrices = roomViewPrices.map { PriceInfo(it.id!!,it.roomView.sleeps,it.price,it.quantity,it.distanceDays,it.roomView.cancellationPolicy,it.timestamp,it.roomView.breakfast,it.roomView.attributes.map(RoomViewAttribute::name).toSet()) }
        every { priceServiceMock.roomPrices(1,any(),2) } returns PageImpl(roomViewPrices)

        mockMvc
            .get("/prices/2/1?page=0&size=10"){
                with(user("1").roles(role.name))
            }
            .andExpect {
                status { isOk() }
                content {
                    jsonPath("$._embedded.priceInfoList",Matchers.hasSize<Any>(2))
                    expectedPrices.forEachIndexed{ i,price ->
                        jsonPath("$._embedded.priceInfoList[$i].id",Matchers.`is`(price.id.toInt()))
                        jsonPath("$._embedded.priceInfoList[$i].sleeps",Matchers.`is`(price.sleeps))
                        jsonPath("$._embedded.priceInfoList[$i].price",Matchers.`is`(price.price))
                        jsonPath("$._embedded.priceInfoList[$i].quantity",Matchers.`is`(price.quantity))
                        jsonPath("$._embedded.priceInfoList[$i].distanceDays",Matchers.`is`(price.distanceDays))
                        jsonPath("$._embedded.priceInfoList[$i].cancellationPolicy",Matchers.`is`(price.cancellationPolicy.name))
                        jsonPath("$._embedded.priceInfoList[$i].breakfastPolicy",Matchers.`is`(price.breakfastPolicy))
                        jsonPath("$._embedded.priceInfoList[$i].attributes",Matchers.hasSize<Any>(0))
                    }
                }
            }.andReturn().apply { println(this.response.contentAsString) }
        verifyAll { priceServiceMock.roomPrices(1,any(),2) }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return 403(Forbidden) when trying to request prices of monitor list which has different owner`(role: Role) {
        every { monitorListServiceMock.isOwner(2,1) } returns false

        mockMvc
            .get("/prices/2/1?page=0&size=10"){
                with(user("1").roles(role.name))
            }.andExpect {
                status { isForbidden() }
            }

        verifyAll {
            monitorListServiceMock.isOwner(2,1)
            priceServiceMock wasNot called
        }
    }

    @MethodSource("mediaTypesWithoutJsonWithRoles")
    @ParameterizedTest
    fun `should return 406(Not Acceptable) response when trying to request prices with unacceptable accept content type`(parameters: Pair<String,Role>) {
        val (invalidContentType,role) = parameters
        mockMvc
            .get("/prices/2/1?page=0&size=10"){
                with(user("1").roles(role.name))
                accept = MediaType.parseMediaType(invalidContentType)
            }.andExpect {
                status { isNotAcceptable() }
            }

        verifyAll { priceServiceMock wasNot called }
    }
}