package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.entity.CancellationPolicy
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class CancellationPolicyExtractorTest(private val cancellationPolicyExtractor: CancellationPolicyExtractor) : AbstractITest(){

    @MethodSource("cancellationPolicies")
    @ParameterizedTest
    fun `should return cancellation policy`(policy: Pair<String,CancellationPolicy>) {
        val (cancellationPolicyAttribute,expectedPolicy) = policy

        cancellationPolicyExtractor.apply(setOf("SOME_ATTRIBUTE",cancellationPolicyAttribute,"SOME_ATTRIBUTE_2")).shouldBe(expectedPolicy)
    }

}