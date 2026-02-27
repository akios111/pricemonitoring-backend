package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.entity.CancellationPolicy
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.function.Function

/**
 * Abstract class that extracts first matched string from set of strings and converts it into type T.
 */
abstract class RegexImportantAttributeExtractor<T>(private val regex: Regex, private val defaultValue: T?) : Function<Set<String>,T?>{

    override fun apply(t: Set<String>): T? {
        val value = t.firstNotNullOfOrNull { attribute -> regex.find(attribute)?.to(attribute) }

        return if(value == null){
            defaultValue
        }else{
            convert(value.second,value.first.value)
        }
    }

    protected abstract fun convert(attribute: String,match: String) : T

}

@Component
class CancellationPolicyExtractor : RegexImportantAttributeExtractor<CancellationPolicy>(Regex("(free cancellation|Non-refundable|Partially refundable|refundable)",RegexOption.IGNORE_CASE),CancellationPolicy.UNKNOWN) {
    override fun convert(attribute: String, match: String): CancellationPolicy {
        return CancellationPolicy.valueOf(match.replace(" ","_").replace("-","_").uppercase())
    }

}

@Component
class BreakfastAttributeExtractor : RegexImportantAttributeExtractor<String>(Regex(".*breakfast.*",RegexOption.IGNORE_CASE),"unknown"){
    override fun convert(attribute: String, match: String): String = match

}

@Component
class CancellationDaysExtractor : RegexImportantAttributeExtractor<LocalDate>(Regex("free cancellation (?:until|before)",RegexOption.IGNORE_CASE),null){
    override fun convert(attribute: String, match: String): LocalDate {
        val cancellationParts = attribute
            .replace("Free cancellation until","",true)
            .replace("Free cancellation before","",true)
            .replace("on","").split(" ")
            .toMutableList()
        cancellationParts.removeIf{ it.isBlank() }

        if(cancellationParts.size == 4){//remove time part
            cancellationParts.removeAt(0)
        }

        val day = try {
            cancellationParts[0].toInt()
        }catch (ex: Exception){
            throw ex
        }
        val month = Month.valueOf(cancellationParts[1].uppercase())
        val year = cancellationParts[2].toInt()


        return LocalDate.of(year,month,day)
    }

}