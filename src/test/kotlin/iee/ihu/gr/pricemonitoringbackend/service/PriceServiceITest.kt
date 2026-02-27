package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.dto.RoomViewData
import iee.ihu.gr.pricemonitoringbackend.entity.MonitorList
import iee.ihu.gr.pricemonitoringbackend.entity.Room
import iee.ihu.gr.pricemonitoringbackend.service.room.RoomViewService
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class PriceServiceITest(private val priceService: PriceService,private val roomViewService: RoomViewService) : AbstractITest() {

    private lateinit var room: Room
    private lateinit var monitorList: MonitorList

    @BeforeEach
    fun setUp() {
        val customer = createUser()
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext().apply { authentication = UsernamePasswordAuthenticationToken(customer.id!!,"") })
        room = createRoom(createHotel())
        monitorList = createMonitorList(room)
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should return empty page when requesting prices of non-existing room`() {
        val idOfNotExistingRoom = 203901293L
        roomRepository.existsById(idOfNotExistingRoom).shouldBeFalse()

        priceService.roomPrices(idOfNotExistingRoom, Pageable.ofSize(10),monitorList.id!!).isEmpty.shouldBeTrue()
    }

    @Test
    fun `should return empty page when requesting prices of room and not existing monitor list`() {
        val idOfNotExistingMonitorList = 2039020L
        monitorListRepository.existsById(idOfNotExistingMonitorList).shouldBeFalse()

        priceService.roomPrices(room.id!!,Pageable.ofSize(10),idOfNotExistingMonitorList).isEmpty.shouldBeTrue()
    }

    @Test
    fun `should return room's prices`() {
        val expectedPrices = setOf(100,300)
        expectedPrices.forEachIndexed { i,price -> roomViewService.createRoomView(RoomViewData(i+1,price,1,monitorList.distances.first(),room)) }

        assertSoftly(priceService.roomPrices(room.id!!, Pageable.ofSize(2),monitorList.id!!)) {
            totalElements shouldBe 2
            totalPages shouldBe 1
            hasNext() shouldBe false
            hasPrevious() shouldBe false
            content.map { it.price }.shouldContainExactlyInAnyOrder(expectedPrices)
            content.forEach { roomViewPrice ->
                roomViewPrice.quantity shouldBe 1
                roomViewPrice.cancellationDays shouldBe 0//taken from attributes,set to 0 if no attribute specifies cancellation.
                roomViewPrice.distanceDays shouldBe monitorList.distances.first()
            }
        }
    }


}