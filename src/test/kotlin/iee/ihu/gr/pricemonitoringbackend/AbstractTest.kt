package iee.ihu.gr.pricemonitoringbackend

import iee.ihu.gr.pricemonitoringbackend.entity.CancellationPolicy
import iee.ihu.gr.pricemonitoringbackend.entity.Role
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month

@ActiveProfiles("test")
abstract class AbstractTest {

    companion object{

        @JvmStatic
        fun invalidHotelUrls() :Array<String> = arrayOf(
            "notAUrl",
            "lackOfProtocol.com",
            "invalidProtocol://something.com",
            "http://something.com", //only https allowed
            "ftp://something.com", //any other protocol other than https is not allowed
            "123123123"
        )

        @JvmStatic
        fun blankStrings() : Array<String> = arrayOf("","   ","\t\t\t","\n","\n\n\n","\r","\r\n")

        @JvmStatic
        fun cancellationDaysData() : Array<Pair<String,LocalDate?>> = arrayOf(
            "Free cancellation before 28 October 2023" to LocalDate.of(2023, Month.OCTOBER,28),
            "free cancellation before 28 October 2023" to LocalDate.of(2023, Month.OCTOBER,28),
            "Free cancellation until 18 December 2023" to LocalDate.of(2023,Month.DECEMBER,18),
            "Free cancellation until 18:00 15 January 2024" to LocalDate.of(2024,Month.JANUARY,15),
            "Free cancellation before 23:59 10 March 2024" to LocalDate.of(2024,Month.MARCH,10),
            "SOME_RANDOM_ATTRIBUTE" to null,
            "breakfast free" to null
        )

        @JvmStatic
        fun cancellationPolicies() : Array<Pair<String,CancellationPolicy>> = arrayOf(
            "Free cancellation" to CancellationPolicy.FREE_CANCELLATION,
            "free cancellation" to CancellationPolicy.FREE_CANCELLATION,
            "Free Cancellation" to CancellationPolicy.FREE_CANCELLATION,
            "Non-refundable" to CancellationPolicy.NON_REFUNDABLE,
            "non-refundable" to CancellationPolicy.NON_REFUNDABLE,
            "Non-Refundable" to CancellationPolicy.NON_REFUNDABLE,
            "Partially refundable" to CancellationPolicy.PARTIALLY_REFUNDABLE,
            "partially refundable" to CancellationPolicy.PARTIALLY_REFUNDABLE,
            "Partially Refundable" to CancellationPolicy.PARTIALLY_REFUNDABLE,
            "Refundable" to CancellationPolicy.REFUNDABLE,
            "refundable" to CancellationPolicy.REFUNDABLE,
            "anything_else" to CancellationPolicy.UNKNOWN,
            "..asasd123" to CancellationPolicy.UNKNOWN,
            "some_new_policy" to CancellationPolicy.UNKNOWN,
            "" to CancellationPolicy.UNKNOWN
        )

        @JvmStatic
        fun breakfastAttributes() : Array<Pair<String,String>> = arrayOf(
            "Breakfast & dinner included" to "Breakfast & dinner included",
            "Breakfast € 10 (optional)" to "Breakfast € 10 (optional)",
            "Continental breakfast included" to "Continental breakfast included",
            "Superb breakfast included" to "Superb breakfast included",
            "some_attribute" to "unknown",
            "partially refundable" to "unknown",
            "asdasdasd" to "unknown",
            "" to "unknown"
        )

        @JvmStatic
        fun allExceptPostMethods() : List<HttpMethod> = HttpMethod.values().filter { method -> method != HttpMethod.POST && method != HttpMethod.TRACE }

        @JvmStatic
        fun allExceptPostMethodsWithRoles() : List<Pair<HttpMethod,Role>> = HttpMethod
            .values()
            .filter { method -> method != HttpMethod.POST && method != HttpMethod.TRACE }
            .flatMap { method -> Role.values().map { role -> method to role } }

        @JvmStatic
        fun allExceptPutMethods() : List<HttpMethod> = HttpMethod.values().filter { method -> method != HttpMethod.PUT && method != HttpMethod.TRACE }

        @JvmStatic
        fun allExceptPostAndGetMethodsWithRoles() : List<Pair<HttpMethod,Role>> =
            HttpMethod.values()
                .filter { method -> method != HttpMethod.POST && method != HttpMethod.GET && method != HttpMethod.TRACE  }
                .flatMap { method -> Role.values().map { role -> method to role } }

        @JvmStatic
        fun allExceptGetMethodsWithRoles() : List<Pair<HttpMethod,Role>> =
            HttpMethod
                .values()
                .filter { method -> method != HttpMethod.GET && method != HttpMethod.TRACE }
                .flatMap { method -> Role.values().map { role -> method to role } }

        @JvmStatic
        fun mediaTypesWithoutJson() : Array<String> = arrayOf(MediaType.APPLICATION_ATOM_XML_VALUE,MediaType.TEXT_HTML_VALUE,MediaType.APPLICATION_FORM_URLENCODED_VALUE)

        @JvmStatic
        fun mediaTypesWithoutJsonWithRoles() : List<Pair<String,Role>> =
            arrayOf(MediaType.APPLICATION_ATOM_XML_VALUE,MediaType.TEXT_HTML_VALUE,MediaType.APPLICATION_FORM_URLENCODED_VALUE).flatMap { contentType -> Role.values().map { role -> contentType to role } }

        @JvmStatic
        fun hotelUrlsWithLanguagePartsAndQueryParts() : List<Pair<String,String>> = listOf(
            "https://test.com/hotel.html?param1=anything" to "https://test.com/hotel.html",
            "https://www.booking.com/hotel/gr/alpe-luxury-accommodation-penelope-collection.en-gb.html" to "https://www.booking.com/hotel/gr/alpe-luxury-accommodation-penelope-collection.html",
            "https://www.booking.com/hotel/gr/alpe-luxury-accommodation-penelope-collection.el.html" to "https://www.booking.com/hotel/gr/alpe-luxury-accommodation-penelope-collection.html",
            "https://www.booking.com/hotel/gr/alpe-luxury-accommodation-penelope-collection.el.html?param1=anything&param2=anything2" to "https://www.booking.com/hotel/gr/alpe-luxury-accommodation-penelope-collection.html"
        )

        @JvmStatic
        fun invalidUrls() : List<String> = listOf("https://url_without_html_ending.htm?test=1","http://urls_that_uses_http.html","https://another_url_without_html_suffix?test=test","http://url_with_http_and_no_html_suffix.jsp?test=test")

        @JvmStatic
        fun invalidEmails() : Array<String> = arrayOf(
            "emmanuel.hibernate.org",
            "emma nuel@hibernate.org",
            "emma(nuel@hibernate.org",
            "emmanuel@",
            "emma\nnuel@hibernate.org",
            "emma@nuel@hibernate.org",
            "emma@nuel@.hibernate.org",
            "Just a string",
            "string",
            "me@",
            "@example.com",
            "me.@example.com",
            ".me@example.com",
            "me@example..com",
            "me\\@example.com",
            "Abc.example.com",
            "A@b@c@example.com",
            "a\"b(c)d,e:f;g<h>i[j\\k]l@example.com",
            "just\"not\"right@example.com",
            "this is\"not\\allowed@example.com",
            "john..doe@example.com",
            "john.doe@example..com"
        )

        @JvmStatic
        fun invalidPasswords() : Array<String> = arrayOf(
            "Not8Lon",
            "NoDigitInPass",
            "0092392391231",//no letters
            "",
            "         "
        )
    }

}