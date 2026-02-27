package iee.ihu.gr.pricemonitoringbackend.controller

import iee.ihu.gr.pricemonitoringbackend.mvc.FAILED_RATE_LIMIT_MESSAGE
import iee.ihu.gr.pricemonitoringbackend.mvc.REMAINING_LIMITS_HEADERS_NAME
import iee.ihu.gr.pricemonitoringbackend.mvc.RETRY_AFTER_SECONDS_HEADER_NAME
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitResult.FailedRateLimitResult
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitResult.SuccessRateLimitResult
import io.mockk.every
import io.mockk.verifyAll
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get

class ThrottlingUnitTest : AbstractControllerUnitTest() {

    @Test
    fun `should return 429 response and set appropriate headers if acquiring call permit fails`() {
        every { rateLimitServiceMock.tryAcquireCallPermit("127.0.0.1") } returns FailedRateLimitResult(10)

        mockMvc
            .get("/test")
            .andExpect {
                status { isTooManyRequests() }
                header {
                    string(RETRY_AFTER_SECONDS_HEADER_NAME,10.toString())
                    doesNotExist(REMAINING_LIMITS_HEADERS_NAME)
                }
                content { string(FAILED_RATE_LIMIT_MESSAGE) }
            }

        verifyAll {
            rateLimitServiceMock.tryAcquireCallPermit("127.0.0.1")
        }
    }

    @Test
    fun `should return 200(OK) response with remaining tokens`() {
        every { rateLimitServiceMock.tryAcquireCallPermit("127.0.0.1") } returns SuccessRateLimitResult(100)

        mockMvc
            .get("/test")
            .andExpect {
                status { isOk() }
                header {
                    string(REMAINING_LIMITS_HEADERS_NAME,"100")
                    doesNotExist(RETRY_AFTER_SECONDS_HEADER_NAME)
                }
            }

        verifyAll { rateLimitServiceMock.tryAcquireCallPermit("127.0.0.1") }
    }
}