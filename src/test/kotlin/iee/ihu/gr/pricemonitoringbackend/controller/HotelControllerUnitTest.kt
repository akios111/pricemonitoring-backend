package iee.ihu.gr.pricemonitoringbackend.controller

import iee.ihu.gr.pricemonitoringbackend.dto.ScrapedData
import iee.ihu.gr.pricemonitoringbackend.dto.HotelInfo
import iee.ihu.gr.pricemonitoringbackend.dto.HotelInfoRequest
import iee.ihu.gr.pricemonitoringbackend.entity.HotelType
import iee.ihu.gr.pricemonitoringbackend.entity.Location
import iee.ihu.gr.pricemonitoringbackend.entity.Role
import iee.ihu.gr.pricemonitoringbackend.toScrappingRequest
import io.mockk.*
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal

class HotelControllerUnitTest : AbstractControllerUnitTest() {

    private val request = HotelInfoRequest("https://test.html")
    private val hotelInfo = HotelInfo("any_name","any_desc", Location("any_address","any_city",10.5F, 12.4F),"https://any_url.com",HotelType.HOTEL, BigDecimal.ONE,1, emptyList())

    @Test
    fun `should return 401(Unauthorized) when trying to request hotel scraping without being authenticated`() {
        mockMvc
            .post("/hotel/info")
            .andExpect { status { isUnauthorized() } }

        verifyAll {
            hotelServiceMock wasNot called
            scrappingServiceMock wasNot called
            scrapperDataPersistServiceMock wasNot called
        }
    }

    @Test
    fun `should return 401(Unauthorized) when trying to request info without being authenticated`() {
        mockMvc
            .get("/hotel/1")
            .andExpect { status { isUnauthorized() } }

        verifyAll {
            hotelServiceMock wasNot called
            scrappingServiceMock wasNot called
            scrapperDataPersistServiceMock wasNot called
        }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should OK(200) response and make a call to scraper and save it`(role: Role) {
        val scrapperResult = ScrapedData("ANY_NAME","ANY_DESCRITPION", Location("ANY_ADDRESS","ANY_CITY",null,null),"http://test.com",HotelType.values().random().name, BigDecimal.ONE)
        every { scrappingServiceMock.requestHotelInfo(request.toScrappingRequest()) } returns scrapperResult
        every { scrapperDataPersistServiceMock.saveScrappingResult(scrapperResult) } returns hotelInfo

        mockMvc
            .post("/hotel/info"){
                with(user("1").roles(role.name))
                contentType = MediaType.APPLICATION_JSON
                content = json.encodeToString(request)
            }.andExpect {
                status { isOk() }
                content { json(json.encodeToString(hotelInfo)) }
            }

        verifyAll {
            hotelServiceMock wasNot called
            scrappingServiceMock.requestHotelInfo(request.toScrappingRequest())
            scrapperDataPersistServiceMock.saveScrappingResult(scrapperResult)
        }

    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return OK(200) response with hotel info`(role: Role) {
        every { hotelServiceMock.getHotelInfo(1) } returns hotelInfo

        mockMvc
            .get("/hotel/1"){
                with(user("1").roles(role.name))
            }.andExpect {
                status { isOk() }
                content { json(json.encodeToString(hotelInfo)) }
            }

        verifyAll {
            hotelServiceMock.getHotelInfo(1)
            scrappingServiceMock wasNot called
            scrapperDataPersistServiceMock wasNot called
        }
    }

    @MethodSource("allExceptPostMethodsWithRoles")
    @ParameterizedTest
    fun `should return 401(Unauthorized) response when trying to request hotel scraping with wrong http method`(parameters: Pair<HttpMethod,Role>) {
        val(invalidHttpMethod,role) = parameters
        mockMvc
            .perform {
                request(invalidHttpMethod,"/hotel/info")
                    .with(user("1").roles(role.name))
                    .contentType(MediaType.APPLICATION_JSON)
                    .buildRequest(it)
            }.andExpect(status().isUnauthorized)

        verifyAll {
            scrappingServiceMock wasNot called
            scrapperDataPersistServiceMock wasNot called
            hotelServiceMock wasNot called
        }
    }


    @MethodSource("allExceptGetMethodsWithRoles")
    @ParameterizedTest
    fun `should return 401(Unauthorized) response when trying to request hotel info with wrong http method`(parameters: Pair<HttpMethod,Role>) {
        val(invalidHttpMethod,role) = parameters

        mockMvc
            .perform {
                request(invalidHttpMethod,"/hotel/1")
                    .with(user("1").roles(role.name))
                    .buildRequest(it)
            }.andExpect(status().isUnauthorized)

        verifyAll {
            scrappingServiceMock wasNot called
            scrapperDataPersistServiceMock wasNot called
            hotelServiceMock wasNot called
        }

    }

    @MethodSource("mediaTypesWithoutJsonWithRoles")
    @ParameterizedTest
    fun `should return 415(Unsupported Media Type) when trying to request hotel scraping with invalid content type in request`(parameters: Pair<String, Role>) {
        val (invalidContentType,role) = parameters
        mockMvc
            .post("/hotel/info"){
                with(user("1").roles(role.name))
                contentType = MediaType.parseMediaType(invalidContentType)
            }.andExpect {
                status { isUnsupportedMediaType() }
                content { string("") }
            }

        verifyAll {
            hotelServiceMock wasNot called
            scrappingServiceMock wasNot called
            scrapperDataPersistServiceMock wasNot called
            hotelServiceMock wasNot called
        }
    }


    @MethodSource("mediaTypesWithoutJsonWithRoles")
    @ParameterizedTest
    fun `should return 406(Not Acceptable) response when requesting hotel scraping and accept content type is not acceptable`(parameters: Pair<String, Role>) {
        val (invalidAcceptContentType,role) = parameters
        mockMvc
            .post("/hotel/info"){
                with(user("1").roles(role.name))
                contentType = MediaType.APPLICATION_JSON
                accept = MediaType.parseMediaType(invalidAcceptContentType)
            }.andExpect {
                status { isNotAcceptable() }
            }

        verifyAll {
            hotelServiceMock wasNot called
            scrappingServiceMock wasNot called
            scrapperDataPersistServiceMock wasNot called
            hotelServiceMock wasNot called
        }
    }


}