package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.clearLanguageInfo
import iee.ihu.gr.pricemonitoringbackend.dto.HotelData
import iee.ihu.gr.pricemonitoringbackend.dto.RoomInfo
import iee.ihu.gr.pricemonitoringbackend.entity.*
import iee.ihu.gr.pricemonitoringbackend.service.hotel.HotelService
import iee.ihu.gr.pricemonitoringbackend.withoutQueryPart
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.optional.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import java.math.BigDecimal
import java.net.URL

class HotelServiceITest @Autowired constructor(private val hotelService: HotelService) : AbstractITest() {

    private val hotelData = HotelData("ANY_NAME", "ANY_DESC", Location("ANY_ADDRSS","ANY_CITY", 10.5F, 10.2F), "https://any.html?query=query", HotelType.HOTEL, BigDecimal.ONE)

    @Test
    fun `should save new hotel`() {
        hotelRepository.findByUrl(URL(hotelData.url)).shouldBeEmpty()

        hotelService.createHotel(hotelData)

        assertSoftly(hotelRepository.findByUrl(URL(hotelData.url.withoutQueryPart().clearLanguageInfo())).get()) {
            hotelType shouldBe hotelData.hotelType
            name shouldBe hotelData.name
            description shouldBe hotelData.description
            location shouldBe hotelData.location
            url shouldBe URL(hotelData.url.withoutQueryPart().clearLanguageInfo())
            score shouldBe hotelData.score
        }
    }

    @MethodSource("hotelUrlsWithLanguagePartsAndQueryParts")
    @ParameterizedTest
    fun `when saving new hotel should clear any query and language parts from the url`(param: Pair<String,String>) {
        val (originalURL,expectedUrl) = param

        hotelService.createHotel(hotelData.copy(url = originalURL))

        hotelRepository.findByUrl(URL(expectedUrl)).get().url.toString() shouldBe expectedUrl
    }

    @Test
    fun `should throw DataIntegrityViolationException when trying to create new hotel with url that is already used`() {
        hotelService.createHotel(hotelData)

        shouldThrow<DataIntegrityViolationException> {
            hotelService.createHotel(hotelData.copy(name = "diff", description = "diff", location = Location("diff","diff", 5.0F, 5.0F), hotelType = HotelType.GUEST_HOUSE, score = BigDecimal.TEN))
        }
    }

    @Test
    fun `should return information about an existing hotel`() {
        val hotel = hotelService.createHotel(hotelData)

        val expectedRooms = listOf(
            roomRepository.save(Room(hotel,"any_desc_1","any_name_1")),
            roomRepository.save(Room(hotel,"any_desc_2","any_name_2"))
        ).map { RoomInfo(it.id!!,it.description,it.name,it.hotel.id!!,it.hotel.name,null,it.attributes.map { attribute -> attribute.name }) }

        assertSoftly(hotelService.getHotelInfo(hotel.id!!)) {
            hotelID shouldBe hotel.id
            name shouldBe hotelData.name
            description shouldBe hotelData.description
            url shouldBe hotelData.url.withoutQueryPart().clearLanguageInfo()
            hotelType shouldBe hotelData.hotelType
            location shouldBe hotelData.location
            score shouldBe hotelData.score
            rooms shouldContainExactlyInAnyOrder expectedRooms
        }
    }

    @Test
    fun `should throw EmptyResultDataAccessException when requesting hotel information that does not exist`() {
        val idOfNotExistingHotel = 1023019L
        hotelRepository.existsById(idOfNotExistingHotel).shouldBeFalse()

        shouldThrow<EmptyResultDataAccessException> { hotelService.getHotelInfo(idOfNotExistingHotel) }
    }

    @Test
    fun `should throw EmptyResultDataAccessException when trying to update hotel that does not exist`() {
        shouldThrow<EmptyResultDataAccessException> { hotelService.updateHotel(hotelData) }
    }

    @Test
    fun `should update hotel`() {
        val id = hotelService.createHotel(hotelData).id
        val update = hotelData.copy(name = "new_name", description = "new_description", location = Location("new_address","new_city", 4.0F, 4.0F), hotelType = HotelType.HOLIDAY_HOME, score = BigDecimal("2.3"))

        hotelService.updateHotel(update)

        assertSoftly(hotelRepository.findById(id!!).get()) {
            hotelType shouldBe update.hotelType
            name shouldBe update.name
            description shouldBe update.description
            location shouldBe update.location
            url shouldBe URL(update.url.withoutQueryPart().clearLanguageInfo())
            score shouldBe update.score
        }
    }

    @MethodSource("blankStrings")
    @ParameterizedTest
    fun `should throw ConstraintViolationException when creating or updating hotel with blank name`(blankHotelName: String) {
        val hotelWithBlankName = hotelData.copy(name = blankHotelName)

        shouldViolate(messageSource.getMessage("HotelData.name.NotBlank.message")) { hotelService.createHotel(hotelWithBlankName) }
        shouldViolate(messageSource.getMessage("HotelData.name.NotBlank.message")){ hotelService.updateHotel(hotelWithBlankName) }
    }

    @MethodSource("blankStrings")
    @ParameterizedTest
    fun `should throw ConstraintViolationException when creating or updating hotel with blank description`(blankDescription: String) {
        val hotelWithBlankDescription = hotelData.copy(description = blankDescription)

        shouldViolate(messageSource.getMessage("HotelData.description.NotBlank.message")) { hotelService.createHotel(hotelWithBlankDescription) }
        shouldViolate(messageSource.getMessage("HotelData.description.NotBlank.message")) { hotelService.updateHotel(hotelWithBlankDescription) }
    }

    @MethodSource("blankStrings")
    @ParameterizedTest
    fun `should throw ConstraintViolationException when creating or updating hotel with blank address`(blankAddress: String) {
        val hotelWithBlankAddress = hotelData.copy(location = hotelData.location.copy(address = blankAddress))

        shouldViolate(messageSource.getMessage("Location.address.NotBlank.message")){ hotelService.createHotel(hotelWithBlankAddress) }
        shouldViolate(messageSource.getMessage("Location.address.NotBlank.message")){ hotelService.updateHotel(hotelWithBlankAddress) }
    }

    @Test
    fun `should throw ConstraintViolationException when creating or updating hotel with address length longer than the allowed maximum`() {
        val address101CharactersLong = StringBuilder(101)
        repeat(101){ address101CharactersLong.append("a") }

        val hotelWithVeryLongAddress = hotelData.copy(location = hotelData.location.copy(address = address101CharactersLong.toString()))

        shouldViolate(messageSource.getMessage("Location.address.Size.message")){ hotelService.createHotel(hotelWithVeryLongAddress) }
        shouldViolate(messageSource.getMessage("Location.address.Size.message")){ hotelService.updateHotel(hotelWithVeryLongAddress) }
    }

    @MethodSource("blankStrings")
    @ParameterizedTest
    fun `should throw ConstraintViolationException when creating or updating hotel blank location's city`(blankCity: String) {
        val hotelWithBlankCity = hotelData.copy(location = hotelData.location.copy(city = blankCity))

        shouldViolate(messageSource.getMessage("Location.city.NotBlank.message")){ hotelService.createHotel(hotelWithBlankCity) }
        shouldViolate(messageSource.getMessage("Location.city.NotBlank.message")){ hotelService.updateHotel(hotelWithBlankCity) }
    }

    @Test
    fun `should throw ConstraintViolationException when creating or updating hotel with city length longer than the allowed maximum`() {
        val city31CharactersLong = StringBuilder()
        repeat(31){ city31CharactersLong.append("a") }

        val hotelWithVeryLongCity = hotelData.copy(location = hotelData.location.copy(city = city31CharactersLong.toString()))

        shouldViolate(messageSource.getMessage("Location.city.Size.message")){ hotelService.createHotel(hotelWithVeryLongCity) }
        shouldViolate(messageSource.getMessage("Location.city.Size.message")){ hotelService.updateHotel(hotelWithVeryLongCity) }
    }

    @MethodSource("invalidHotelUrls")
    @ParameterizedTest
    fun `should throw ConstraintViolationException when creating or updating hotel with invalid url`(invalidUrl: String) {
        val hotelWithInvalidUrl = hotelData.copy(url = invalidUrl)

        shouldViolate(messageSource.getMessage("HotelData.url.URL.message")) { hotelService.createHotel(hotelWithInvalidUrl) }
        shouldViolate(messageSource.getMessage("HotelData.url.URL.message")){ hotelService.updateHotel(hotelWithInvalidUrl) }
    }

    @Test
    fun `should throw ConstraintViolationException when creating or updating hotel with negative score value`() {
        val hotelWithNegativeScore = hotelData.copy(score = BigDecimal("-0.1"))

        shouldViolate(messageSource.getMessage("HotelData.score.DecimalMin.message")){ hotelService.createHotel(hotelWithNegativeScore) }
        shouldViolate(messageSource.getMessage("HotelData.score.DecimalMin.message")){ hotelService.updateHotel(hotelWithNegativeScore) }
    }

    @Test
    fun `should throw ConstraintViolationException when creating or updating hotel with score higher than 10`() {
        val hotelWithExceedingScore = hotelData.copy(score = BigDecimal("10.1"))

        shouldViolate(messageSource.getMessage("HotelData.score.DecimalMax.message")) { hotelService.createHotel(hotelWithExceedingScore) }
        shouldViolate(messageSource.getMessage("HotelData.score.DecimalMax.message")){ hotelService.updateHotel(hotelWithExceedingScore) }

    }

}