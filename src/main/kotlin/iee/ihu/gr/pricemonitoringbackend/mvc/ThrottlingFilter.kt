package iee.ihu.gr.pricemonitoringbackend.mvc

import iee.ihu.gr.pricemonitoringbackend.ipAddress
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitResult
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitResult.FailedRateLimitResult
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitResult.SuccessRateLimitResult
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitService
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

const val RETRY_AFTER_SECONDS_HEADER_NAME = "X-Rate-Limit-Retry-After-Seconds"
const val REMAINING_LIMITS_HEADERS_NAME = "X-Rate-Limit-Remaining"
const val FAILED_RATE_LIMIT_MESSAGE = "Too many requests."

@Component
class ThrottlingFilter(private val rateLimitService: RateLimitService) : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse
        val ip = httpRequest.ipAddress()

        when(val result = rateLimitService.tryAcquireCallPermit(ip)){
            is SuccessRateLimitResult -> {
                httpResponse.setHeader(REMAINING_LIMITS_HEADERS_NAME,result.remaining.toString())
                chain.doFilter(request,response)
            }
            is FailedRateLimitResult -> {
                httpResponse.status = 429
                httpResponse.setHeader(RETRY_AFTER_SECONDS_HEADER_NAME,result.secondsUntilRefill.toString())
                httpResponse.contentType = "text/plain"
                httpResponse.writer.append(FAILED_RATE_LIMIT_MESSAGE)
            }
        }

    }


}