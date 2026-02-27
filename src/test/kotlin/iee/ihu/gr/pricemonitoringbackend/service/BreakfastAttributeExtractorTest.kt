package iee.ihu.gr.pricemonitoringbackend.service

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class BreakfastAttributeExtractorTest(private val breakfastAttributeExtractor: BreakfastAttributeExtractor) : AbstractITest() {

    @MethodSource("breakfastAttributes")
    @ParameterizedTest
    fun `should return breakfast attribute`(breakfast: Pair<String,String>) {
        val (breakfastAttribute,expectedBreakfast) = breakfast

        breakfastAttributeExtractor.apply(setOf("SOME_ATTRIBUTE",breakfastAttribute,"ANOTHER_ATTRIBUTE")).shouldBe(expectedBreakfast)
    }
}