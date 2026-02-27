package iee.ihu.gr.pricemonitoringbackend.service

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

class CancellationDaysExtractorTest(private val cancellationDaysExtractor: CancellationDaysExtractor) : AbstractITest() {

    @MethodSource("cancellationDaysData")
    @ParameterizedTest
    fun `should return cancellation day attribute or null`(cancellationData: Pair<String,LocalDate?>) {
        val (cancellationAttribute,expectedResult) = cancellationData
        cancellationDaysExtractor.apply(setOf("SOME_ATTRIBUTE",cancellationAttribute,"ANOTHER_ATTRIBUTE")).shouldBe(expectedResult)
    }
}