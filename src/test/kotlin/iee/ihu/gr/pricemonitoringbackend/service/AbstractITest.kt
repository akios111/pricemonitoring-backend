package iee.ihu.gr.pricemonitoringbackend.service

import com.ninjasquad.springmockk.MockkBean
import iee.ihu.gr.pricemonitoringbackend.AbstractTest
import iee.ihu.gr.pricemonitoringbackend.dto.*
import iee.ihu.gr.pricemonitoringbackend.entity.*
import iee.ihu.gr.pricemonitoringbackend.repository.*
import iee.ihu.gr.pricemonitoringbackend.service.email.EmailService
import iee.ihu.gr.pricemonitoringbackend.service.hotel.HotelService
import iee.ihu.gr.pricemonitoringbackend.service.monitor.MonitorListService
import iee.ihu.gr.pricemonitoringbackend.service.room.RoomService
import iee.ihu.gr.pricemonitoringbackend.service.room.RoomViewService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import jakarta.validation.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestConstructor
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random


@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
@Testcontainers
@AutoConfigureTestEntityManager
@SpringBootTest
abstract class AbstractITest : AbstractTest(){

    @Autowired
    protected lateinit var messageSource: MessageSource

    @Autowired
    protected lateinit var hotelRepository: HotelRepository

    @Autowired
    protected lateinit var roomRepository: RoomRepository

    @Autowired
    protected lateinit var roomAttributeRepository: RoomAttributeRepository

    @Autowired
    protected lateinit var roomViewAttributeRepository: RoomViewAttributeRepository

    @Autowired
    protected lateinit var roomViewRepository: RoomViewRepository

    @Autowired
    protected lateinit var priceRepository: PriceRepository

    @Autowired
    protected lateinit var userRepository: UserRepository

    @Autowired
    protected lateinit var monitorListRepository: MonitorListRepository

    @Autowired
    protected lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var hotelService: HotelService

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var monitorListService: MonitorListService

    @Autowired
    private lateinit var roomViewService: RoomViewService

    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    protected lateinit var transactionalTemplate: TransactionTemplate

    @Autowired
    protected lateinit var base64Decoder: Base64.Decoder

    protected val base64Encoder: Base64.Encoder = Base64.getEncoder()

    @MockkBean
    protected lateinit var emailServiceMock: EmailService

    @Value("\${secret.key}")
    protected lateinit var key: String

    @Value("\${token.issuer}")
    protected lateinit var issuer: String

    protected fun cleanDB(){
        transactionalTemplate.execute {
            JdbcTestUtils.deleteFromTables(jdbcTemplate, *tablesOrderedByDelete())
        }
    }

    companion object{

        private val mysqlContainer = MySQLContainer("mysql:latest").withReuse(true)
        @Container
        private val rabbitmqContainer = RabbitMQContainer("rabbitmq:latest")
        @JvmStatic
        @DynamicPropertySource
        fun propertyInit(registry: DynamicPropertyRegistry){
            mysqlContainer.start()
            registry.add("spring.datasource.username"){ mysqlContainer.username }
            registry.add("spring.datasource.password"){ mysqlContainer.password }
            registry.add("spring.datasource.url"){ mysqlContainer.jdbcUrl }
            registry.add("spring.rabbitmq.port"){ rabbitmqContainer.amqpPort }
        }

        fun tablesOrderedByDelete() = arrayOf(
                "prices","monitor_list_details","monitor_list","hotel_room_choices_owned_attributes_AUD",
                "hotel_room_choices_owned_attributes", "hotel_room_choices_AUD", "hotel_room_choices","hotel_rooms_owned_attributes_AUD",
                "hotel_rooms_owned_attributes","hotel_rooms_AUD","hotel_room_attributes","hotel_rooms","room_choice_attributes",
                "hotels","REVINFO","users"
        )

    }

    protected fun createHotel() : Hotel {
        return hotelService.createHotel(HotelData("ANY_NAME","ANY_DESC",Location("ANY_ADDRESS","ANY_CITY", Random.nextFloat(),Random.nextFloat()),"https://${UUID.randomUUID()}.html?query=query",HotelType.values().random(),BigDecimal(Random.nextInt(10))))
    }

    protected fun createRoomAttributes(amount: Int = 1) : List<RoomAttribute>{
        val list = mutableListOf<RoomAttribute>()
        repeat(amount){
            list.add(roomAttributeRepository.save(RoomAttribute("ATTRIBUTE_${it}")))
        }
        return list
    }

    protected fun createRoomViewAttributes(roomViewData: RoomViewData) : List<RoomViewAttribute>{
        val list = mutableListOf<RoomViewAttribute>()
        repeat(4){
            list.add(roomViewAttributeRepository.save(RoomViewAttribute("ROOM_VIEW_ATTRIBUTE_${it}",)))
        }
        list.add(roomViewAttributeRepository.save(RoomViewAttribute(CancellationPolicy.FREE_CANCELLATION.name)))
        list.add(roomViewAttributeRepository.save(RoomViewAttribute("Free cancellation before ${LocalDate.now().plusDays(roomViewData.distanceDays+1L).format(DateTimeFormatter.ofPattern("d MMMM yyyy"))}")))
        return list
    }

    protected fun createRoom(hotel: Hotel) : Room {
        return roomService.createRoom(RoomData(hotel,"ANY_DESCRIPTION",UUID.randomUUID().toString().substring(0,20)))
    }

    protected fun createMonitorList(room: Room) = monitorListService.createMonitorList(MonitorListCreateForm(setOf(room.id!!),"ANY",setOf(30)))

    protected fun createRoomView(room: Room) : RoomView{
        return roomViewService.createRoomView(RoomViewData(Random.nextInt(1,10),Random.nextInt(1,1000),Random.nextInt(1,10),Random.nextInt(1,10),room))
    }

    protected fun createUser(role: Role = Role.USER) : User{
        return userRepository.save(User(Email("${UUID.randomUUID().toString().substring(1..5)}@email.com"),"any","ANY_PASS",role))
    }

    protected fun shouldViolate(expectedMessage: String, block: () -> Unit) {
        val violations = shouldThrow<ConstraintViolationException>(block).constraintViolations
        violations shouldHaveSize 1
        violations.iterator().next().message shouldBe expectedMessage
    }

    protected fun MessageSource.getMessage(code: String) : String = getMessage(code, emptyArray(),LocaleContextHolder.getLocale())

}