package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.dto.RoomViewData
import iee.ihu.gr.pricemonitoringbackend.entity.*
import iee.ihu.gr.pricemonitoringbackend.service.room.RoomViewService
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.optional.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class RoomViewServiceITest @Autowired constructor(
    private val roomViewService: RoomViewService,
    private val cancellationPolicyExtractor: CancellationPolicyExtractor,
    private val breakfastAttributeExtractor: BreakfastAttributeExtractor,
    private val cancellationDaysExtractor: CancellationDaysExtractor
): AbstractITest(){

    private lateinit var hotel: Hotel
    private lateinit var room: Room
    private lateinit var roomViewData: RoomViewData
    private lateinit var monitorList: MonitorList

    @BeforeEach
    fun prepareTestContext(){
        val customer = createUser()
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext().apply { authentication = UsernamePasswordAuthenticationToken(customer.id!!,"") })
        val initData = initData()
        hotel = initData.hotel
        room = initData.room
        roomViewData = initData.roomViewData
        monitorList = initData.monitorList
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should create new room view and save corresponding price`() {
        roomViewRepository.findRoomViewBySleepsAndCancellationPolicyAndBreakfastAndRoom(roomViewData.sleeps,CancellationPolicy.FREE_CANCELLATION,"unknown",room).shouldBeEmpty()

        roomViewService.createRoomView(roomViewData)

        assertSoftly(roomViewRepository.findRoomViewBySleepsAndCancellationPolicyAndBreakfastAndRoom(roomViewData.sleeps,CancellationPolicy.FREE_CANCELLATION,"unknown",room).get()) {
            it.room shouldBe room
            breakfast shouldBe breakfastAttributeExtractor.apply(roomViewData.attributes.map(RoomViewAttribute::name).toSet())
            attributes shouldContainExactlyInAnyOrder roomViewData.attributes
            sleeps shouldBe roomViewData.sleeps
            cancellationPolicy shouldBe cancellationPolicyExtractor.apply(roomViewData.attributes.map(RoomViewAttribute::name).toSet())
        }
        val prices = priceRepository.findByIdIn(priceRepository.roomPrices(room.id!!,monitorList.id!!,Pageable.unpaged()).content)
        prices.shouldHaveSize(1)
        assertSoftly(prices[0]) {
            it.roomView shouldBe roomView
            distanceDays shouldBe roomViewData.distanceDays
            price shouldBe roomViewData.price
            quantity shouldBe roomViewData.quantity
            cancellationDays shouldBe ChronoUnit.DAYS.between(timestamp.toLocalDate().plusDays(distanceDays.toLong()),cancellationDaysExtractor.apply(roomViewData.attributes.map(RoomViewAttribute::name).toSet()))
        }
    }

    /**
     * Room view is considered duplicate if it has the same number of sleeps,same breakfast policy and same cancellation policy
     */
    @Test
    fun `should throw DataIntegrityViolationException when trying to save duplicate room view`() {
        roomViewService.createRoomView(roomViewData)

        shouldThrow<DataIntegrityViolationException> { roomViewService.createRoomView(roomViewData.copy(price = 10, quantity = 10, distanceDays = 20)) }
    }

    @Test
    fun `should throw EmptyResultDataAccessException when trying to update non-existing room view because of different sleeps`() {
        roomViewService.createRoomView(roomViewData)

        shouldThrow<EmptyResultDataAccessException> { roomViewService.updateRoomView(roomViewData.copy(sleeps = 100)) }
    }

    @Test
    fun `should throw EmptyResultDataAccessException when trying to update non-existing room view because of different breakfast policy`() {
        //initial room view has unknown breakfast
        roomViewService.createRoomView(roomViewData)
        //removed add different breakfast attribute
        val attributesWithDifferentBreakfastPolicy = roomViewData.attributes.toMutableList().apply {
            add(roomViewAttributeRepository.save(RoomViewAttribute("free breakfast")))
        }

        shouldThrow<EmptyResultDataAccessException> { roomViewService.updateRoomView(roomViewData.copy(attributes = sortedSetOf<RoomViewAttribute>().apply { addAll(attributesWithDifferentBreakfastPolicy) } )) }
    }

    @Test
    fun `should throw EmptyResultDataAccessException when trying to update non-existing room view because of different cancellation policy`() {
        roomViewService.createRoomView(roomViewData)
        val attributesWithDifferentCancellationPolicy = roomViewData.attributes.toMutableList().apply {
            remove(roomViewAttributeRepository.findByName(CancellationPolicy.FREE_CANCELLATION.name).get())
            add(roomViewAttributeRepository.save(RoomViewAttribute(CancellationPolicy.NON_REFUNDABLE.name)))
        }

        roomViewService.updateRoomView(roomViewData.copy(attributes = sortedSetOf<RoomViewAttribute>().apply { addAll(attributesWithDifferentCancellationPolicy) } ))
    }

    @Test
    fun `should update room view`() {
        roomViewService.createRoomView(roomViewData)
        val newAttributes = roomViewData.attributes.toMutableList().apply {
            remove(roomViewAttributeRepository.findByName("ROOM_VIEW_ATTRIBUTE_1").get())
            add(roomViewAttributeRepository.save(RoomViewAttribute("NEW_ROOM_VIEW_ATTRIBUTE")))
        }
        val update = roomViewData.copy(price = 999, quantity = 999,attributes = sortedSetOf<RoomViewAttribute>().apply { addAll(newAttributes) } )

        roomViewService.updateRoomView(update)

        assertSoftly(roomViewRepository.findRoomViewBySleepsAndCancellationPolicyAndBreakfastAndRoom(roomViewData.sleeps,CancellationPolicy.FREE_CANCELLATION,"unknown",room).get()) {
            attributes shouldContainExactlyInAnyOrder update.attributes
        }
        val prices = priceRepository.findByIdIn(priceRepository.roomPrices(room.id!!,monitorList.id!!,Pageable.unpaged()).content)
        prices.shouldHaveSize(2) // contains old and new price
        assertSoftly(prices.find { it.price == update.price }!!) {
            distanceDays shouldBe update.distanceDays
            price shouldBe update.price
            quantity shouldBe update.quantity
        }
    }

    @ValueSource(ints = [0,-1,-2])
    @ParameterizedTest
    fun `should throw ConstraintViolationException when trying to insert or update room view with non-positive sleeps`(invalidSleeps: Int) {
        val roomViewDataWithNonPositiveSleeps = roomViewData.copy(sleeps = invalidSleeps)

        shouldViolate(messageSource.getMessage("RoomViewData.sleeps.Positive.message")){ roomViewService.createRoomView(roomViewDataWithNonPositiveSleeps) }
        shouldViolate(messageSource.getMessage("RoomViewData.sleeps.Positive.message")){ roomViewService.updateRoomView(roomViewDataWithNonPositiveSleeps) }
    }

    @ValueSource(ints = [0,-1,-2])
    @ParameterizedTest
    fun `should throw ConstraintViolationException when trying to insert or update room view with non-positive price`(invalidPrice: Int) {
        val roomViewDataWithNonPositivePrice = roomViewData.copy(price = invalidPrice)

        shouldViolate(messageSource.getMessage("RoomViewData.price.Positive.message")){ roomViewService.createRoomView(roomViewDataWithNonPositivePrice) }
        shouldViolate(messageSource.getMessage("RoomViewData.price.Positive.message")){ roomViewService.updateRoomView(roomViewDataWithNonPositivePrice) }
    }

    @Test
    fun `should throw ConstraintViolationException when trying to insert or update room view with negative quantity`() {
        val roomViewDataWithNegativeQuantity = roomViewData.copy(quantity = -1)

        shouldViolate(messageSource.getMessage("RoomViewData.quantity.PositiveOrZero.message")){ roomViewService.createRoomView(roomViewDataWithNegativeQuantity) }
        shouldViolate(messageSource.getMessage("RoomViewData.quantity.PositiveOrZero.message")){ roomViewService.updateRoomView(roomViewDataWithNegativeQuantity) }
    }

    @ValueSource(ints = [0,-1,-2])
    @ParameterizedTest
    fun `should throw ConstraintViolationException when trying to insert or update room view with non-positive distance days`(invalidDistanceDays: Int) {
        val roomViewDataWithNonPositiveDistanceDays = roomViewData.copy(distanceDays = invalidDistanceDays)

        shouldViolate(messageSource.getMessage("RoomViewData.distanceDays.Positive.message")){ roomViewService.createRoomView(roomViewDataWithNonPositiveDistanceDays) }
        shouldViolate(messageSource.getMessage("RoomViewData.distanceDays.Positive.message")){ roomViewService.updateRoomView(roomViewDataWithNonPositiveDistanceDays) }
    }

    private fun initData() : InitData {
        val hotel = createHotel()
        val room = createRoom(hotel)
        val monitorList = createMonitorList(room)
        val roomViewData = RoomViewData(Random.nextInt(1,10),Random.nextInt(100,1000),Random.nextInt(1,10),monitorList.distances.first(),room)


        return InitData(hotel,room,roomViewData.copy(attributes = sortedSetOf<RoomViewAttribute>().apply { addAll(createRoomViewAttributes(roomViewData)) }),monitorList)
    }

    private data class InitData(val hotel: Hotel,val room: Room,val roomViewData: RoomViewData,val monitorList: MonitorList)
}