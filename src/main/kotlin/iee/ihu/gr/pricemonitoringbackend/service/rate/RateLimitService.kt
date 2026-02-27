package iee.ihu.gr.pricemonitoringbackend.service.rate

interface RateLimitService {

    fun tryAcquireEmailVerificationPermit(ip: String) : Boolean

    fun tryAcquirePasswordResetPermit(ip: String) : Boolean

    fun tryAcquireCallPermit(ip: String) : RateLimitResult
}

sealed class RateLimitResult{

    class SuccessRateLimitResult(val remaining: Long) : RateLimitResult()

    class FailedRateLimitResult(val secondsUntilRefill: Long) : RateLimitResult()

}
