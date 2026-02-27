package iee.ihu.gr.pricemonitoringbackend.service.rate

import iee.ihu.gr.pricemonitoringbackend.entity.RateLimitRefillStrategy
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitResult.FailedRateLimitResult
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitResult.SuccessRateLimitResult
import io.github.bucket4j.BandwidthBuilder.BandwidthBuilderBuildStage
import io.github.bucket4j.BandwidthBuilder.BandwidthBuilderCapacityStage
import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Duration.ofDays
import java.util.concurrent.TimeUnit


@Service
class RateLimitServiceBucket4j(
    private val ipProxyManager: ProxyManager<String>,
    private val rateLimitProperties: ConfigurationPropertiesRateLimit
) : RateLimitService {

    override fun tryAcquireEmailVerificationPermit(ip: String): Boolean {
        val bucket = ipProxyManager
            .builder()
            .build("$ip-email"){
                BucketConfiguration.builder().addLimit { limit ->
                    limit.configure(rateLimitProperties.email)
                }.build()
            }

        return bucket.tryConsume(1)
    }

    override fun tryAcquirePasswordResetPermit(ip: String): Boolean {
        val bucket = ipProxyManager
            .builder()
            .build("$ip-password"){
                BucketConfiguration.builder().addLimit { limit -> limit.configure(rateLimitProperties.password) }.build()
            }

        return bucket.tryConsume(1)
    }

    override fun tryAcquireCallPermit(ip: String): RateLimitResult {
        val bucket = ipProxyManager
            .builder()
            .build("$ip-general"){
                BucketConfiguration.builder()
                    .addLimit { limit -> limit.configure(rateLimitProperties.api) }
                    .build()
            }

        val result = bucket.tryConsumeAndReturnRemaining(1)

        return if(result.isConsumed){
            SuccessRateLimitResult(result.remainingTokens)
        }else{
            FailedRateLimitResult(TimeUnit.NANOSECONDS.toSeconds(result.nanosToWaitForRefill))
        }
    }

    private fun BandwidthBuilderCapacityStage.configure(rateLimit: RateLimit) : BandwidthBuilderBuildStage = when(rateLimit.refillStrategy){
        RateLimitRefillStrategy.INTERVAL -> capacity(rateLimit.capacity).refillIntervally(rateLimit.refillTokens, rateLimit.refillPeriod)
        RateLimitRefillStrategy.GREEDY -> capacity(rateLimit.capacity).refillGreedy(rateLimit.refillTokens, rateLimit.refillPeriod)
    }

}