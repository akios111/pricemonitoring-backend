package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.service.rate.ConfigurationPropertiesRateLimit
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitResult
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitResult.FailedRateLimitResult
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitResult.SuccessRateLimitResult
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitService
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.support.JdbcUtils
import org.springframework.test.context.transaction.AfterTransaction
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import javax.sql.DataSource

@Transactional(propagation = Propagation.NEVER)
class RateLimitServiceITest(private val rateLimitService: RateLimitService) : AbstractITest() {

    @Autowired
    private lateinit var rateLimitProperties: ConfigurationPropertiesRateLimit

    @Test
    fun `should return failure after reaching api rate limit`() {
        repeat((rateLimitProperties.api.capacity).toInt()){
            rateLimitService.tryAcquireCallPermit("127.0.0.1").shouldBeInstanceOf<SuccessRateLimitResult>()
        }

        rateLimitService.tryAcquireCallPermit("127.0.0.1").shouldBeInstanceOf<FailedRateLimitResult>()
    }

    @Test
    fun `should return failure after reaching email verification limit`() {
        repeat((rateLimitProperties.email.capacity).toInt()){
            rateLimitService.tryAcquireEmailVerificationPermit("127.0.0.1").shouldBeTrue()
        }

        rateLimitService.tryAcquireEmailVerificationPermit("127.0.0.1").shouldBeFalse()
    }

    @Test
    fun `should return failure after reaching password reset limit`() {
        repeat((rateLimitProperties.password.capacity).toInt()){
            rateLimitService.tryAcquirePasswordResetPermit("127.0.0.1").shouldBeTrue()
        }

        rateLimitService.tryAcquirePasswordResetPermit("127.0.0.1").shouldBeFalse()
    }

    @Test
    fun `should not interfere ip addresses when requesting api permit`() {
        repeat((rateLimitProperties.api.capacity).toInt()){
            rateLimitService.tryAcquireCallPermit("127.0.0.1").shouldBeInstanceOf<SuccessRateLimitResult>()
        }

        rateLimitService.tryAcquireCallPermit("127.0.0.2").shouldBeInstanceOf<SuccessRateLimitResult>()
        rateLimitService.tryAcquireCallPermit("127.0.0.1").shouldBeInstanceOf<FailedRateLimitResult>()
    }

    @Test
    fun `should not interfere ip addresses when requesting email verification permit`() {
        repeat((rateLimitProperties.email.capacity).toInt()){
            rateLimitService.tryAcquireEmailVerificationPermit("127.0.0.1").shouldBeTrue()
        }

        rateLimitService.tryAcquireEmailVerificationPermit("127.0.0.2").shouldBeTrue()
        rateLimitService.tryAcquireEmailVerificationPermit("127.0.0.1").shouldBeFalse()
    }

    @Test
    fun `should not interfere ip addresses when requesting password rest permit`() {
        repeat((rateLimitProperties.password.capacity).toInt()){
            rateLimitService.tryAcquirePasswordResetPermit("127.0.0.1").shouldBeTrue()
        }

        rateLimitService.tryAcquirePasswordResetPermit("127.0.0.2").shouldBeTrue()
        rateLimitService.tryAcquirePasswordResetPermit("127.0.0.1").shouldBeFalse()
    }

    @AfterEach
    fun clearDB(){
        transactionalTemplate.execute {
            jdbcTemplate.update("DELETE FROM ip_buckets")
        }
    }

}