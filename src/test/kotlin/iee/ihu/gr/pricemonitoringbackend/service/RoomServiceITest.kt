package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.dto.RoomData
import iee.ihu.gr.pricemonitoringbackend.entity.Hotel
import iee.ihu.gr.pricemonitoringbackend.service.room.RoomService
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.optional.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException

class RoomServiceITest @Autowired constructor(private val roomService: RoomService) : AbstractITest() {

    private lateinit var existingHotel: Hotel

    @BeforeEach
    fun prepareTestContext(){
        existingHotel = createHotel()
    }

    @Test
    fun `should create new room`() {
        val roomInfo = RoomData(existingHotel,"any_desc","any_name", createRoomAttributes(5))
        roomRepository.findByHotelAndName(existingHotel,roomInfo.name).shouldBeEmpty()

        roomService.createRoom(roomInfo)

        assertSoftly(roomRepository.findByHotelAndName(existingHotel,roomInfo.name).get()) {
            attributes shouldContainExactlyInAnyOrder roomInfo.attributes
            description shouldBe roomInfo.description
            name shouldBe roomInfo.name
            hotel shouldBe existingHotel
        }

    }

    @Test
    fun `should throw DataIntegrityViolationException when trying to create room with name and there is already room with same name associated with the same hotel`() {
        val roomInfo = RoomData(existingHotel,"any_desc","any_name")
        roomService.createRoom(roomInfo)

        shouldThrow<DataIntegrityViolationException> { roomService.createRoom(roomInfo.copy(description = "diff")) }
    }

    @Test
    fun `should throw EmptyResultDataAccessException when trying to update room that does not exist`() {
        val nonExistingRoom = RoomData(existingHotel,"any_des","any_name")

        shouldThrow<EmptyResultDataAccessException> { roomService.updateRoom(nonExistingRoom) }
    }

    @Test
    fun `should throw EmptyResultDataAccessException when trying to update room that belongs to different hotel`() {
        val hotel1 = createHotel()
        val hotel2 = createHotel()
        val roomInfo = RoomData(hotel1,"any_desc","any_name")
        roomService.createRoom(roomInfo)

        shouldThrow<EmptyResultDataAccessException> { roomService.updateRoom(roomInfo.copy(hotel = hotel2)) }
    }

    @Test
    fun `should update room`() {
        val roomHotel = createHotel()
        val roomInfo = RoomData(roomHotel,"any_desc","any_name")
        roomService.createRoom(roomInfo)
        val update = roomInfo.copy(description = "new_desc", attributes = createRoomAttributes(3))

        roomService.updateRoom(update)

        assertSoftly(roomRepository.findByHotelAndName(roomHotel,update.name).get()) {
            attributes shouldContainExactlyInAnyOrder update.attributes
            description shouldBe update.description
            hotel shouldBe roomHotel
        }
    }

    @MethodSource("blankStrings")
    @ParameterizedTest
    fun `should throw ConstraintViolationException when trying to create or update room with blankName`(blankName: String) {
        val roomWithBlankName = RoomData(existingHotel,"ANY_DESC",blankName)

        shouldViolate(messageSource.getMessage("RoomData.name.NotBlank.message")){ roomService.createRoom(roomWithBlankName) }
        shouldViolate(messageSource.getMessage("RoomData.name.NotBlank.message")){ roomService.updateRoom(roomWithBlankName) }
    }

}