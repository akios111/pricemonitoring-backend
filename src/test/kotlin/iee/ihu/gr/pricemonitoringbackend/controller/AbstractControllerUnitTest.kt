package iee.ihu.gr.pricemonitoringbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import iee.ihu.gr.pricemonitoringbackend.AbstractTest
import iee.ihu.gr.pricemonitoringbackend.configuration.InfrastructureConfiguration
import iee.ihu.gr.pricemonitoringbackend.configuration.SecurityConfiguration
import iee.ihu.gr.pricemonitoringbackend.service.*
import iee.ihu.gr.pricemonitoringbackend.service.email.EmailService
import iee.ihu.gr.pricemonitoringbackend.service.hotel.HotelService
import iee.ihu.gr.pricemonitoringbackend.service.monitor.MonitorListService
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitResult.SuccessRateLimitResult
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitService
import iee.ihu.gr.pricemonitoringbackend.service.room.RoomService
import iee.ihu.gr.pricemonitoringbackend.service.subscription.SubscriptionService
import iee.ihu.gr.pricemonitoringbackend.service.user.CustomerService
import io.mockk.every
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.context.WebApplicationContext
@Import(InfrastructureConfiguration::class,SecurityConfiguration::class)
@WebMvcTest
abstract class AbstractControllerUnitTest : AbstractTest()  {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    protected val json: Json = Json.Default

    @Autowired
    protected lateinit var messageSource: MessageSource

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @MockkBean
    protected lateinit var customerServiceMock: CustomerService

    @MockkBean
    protected lateinit var hotelServiceMock: HotelService

    @MockkBean
    protected lateinit var scrappingServiceMock: ScrappingService

    @MockkBean
    protected lateinit var monitorListServiceMock: MonitorListService

    @MockkBean
    protected lateinit var priceServiceMock: PriceService

    @MockkBean
    protected lateinit var roomServiceMock: RoomService

    @MockkBean
    protected lateinit var scrapperDataPersistServiceMock: ScrapperDataPersistService

    @MockkBean
    protected lateinit var emailServiceMock: EmailService

    @MockkBean
    protected lateinit var subscriptionServiceMock: SubscriptionService

    @MockkBean
    protected lateinit var jwtGeneratorMock: JWTGenerator

    @MockkBean
    protected lateinit var rateLimitServiceMock: RateLimitService

    @MockkBean
    protected lateinit var statisticServiceMock: StatisticService


    @BeforeEach
    fun setUp() {
        every { rateLimitServiceMock.tryAcquireCallPermit("127.0.0.1") } returns SuccessRateLimitResult(1)
    }
}