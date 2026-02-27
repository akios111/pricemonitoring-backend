package iee.ihu.gr.pricemonitoringbackend.service.rate

import iee.ihu.gr.pricemonitoringbackend.entity.RateLimitRefillStrategy
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@ConfigurationProperties("rate.limit")
class ConfigurationPropertiesRateLimit(val email: RateLimit,val password: RateLimit,val api: RateLimit)

data class RateLimit(val capacity: Long,val refillStrategy: RateLimitRefillStrategy,val refillTokens: Long,val refillPeriod: Duration)