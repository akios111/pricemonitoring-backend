package iee.ihu.gr.pricemonitoringbackend

import iee.ihu.gr.pricemonitoringbackend.entity.*
import iee.ihu.gr.pricemonitoringbackend.service.AbstractITest
import org.junit.jupiter.api.Test
import org.springframework.test.annotation.Commit
import org.springframework.test.context.transaction.AfterTransaction
import org.springframework.test.context.transaction.BeforeTransaction
import java.math.BigDecimal
import java.net.URL

class Examples : AbstractITest() {

    private var hotelID = 0L
    private var roomID = 0L

    @BeforeTransaction
    fun beforeTransaction(){
        val hotel = hotelRepository.save(Hotel(URL("https://test.com"),"TEST","TEST", Location("TEST","TEST"),HotelType.HOTEL, BigDecimal.ONE))
        val room = roomRepository.save(Room(hotel,"TEST","TEST"))
        repeat(3){
            val attribute = roomAttributeRepository.save(RoomAttribute("TEST_${it}"))
            room.attributes.add(attribute)
        }
        roomRepository.save(room)
        hotelID = hotel.id!!
        roomID = room.id!!
    }

    @Test
    fun `persistent context working as first level cache example`() {

        hotelRepository.findById(hotelID)

        hotelRepository.findById(hotelID)

    }

    @Commit
    @Test
    fun `dirty checking example`() {
        val unchangedHotel = hotelRepository.save(Hotel(URL("https://test1.com"),"TEST","TEST", Location("TEST","TEST"),HotelType.HOTEL, BigDecimal.ONE))
        val changedHotel = hotelRepository.save(Hotel(URL("https://test2.com"),"TEST","TEST", Location("TEST","TEST"),HotelType.HOTEL, BigDecimal.ONE))

        changedHotel.url = URL("https://new_url.com")
    }

    @Test
    fun `lazy collection loading example`() {
        val room = roomRepository.findById(roomID).get()

        //first collection access
        println(room.attributes)
    }

    @Test
    fun `debugging for implementation details of spring data repository`() {
        userRepository.findByEmail("test")
    }

    @Test
    fun `example jpql without fetch`() {
        println("Start example jpql without fetch.")
        val room = entityManager.entityManager.createQuery("SELECT r FROM Room r JOIN r.hotel WHERE r.id = $roomID",Room::class.java).singleResult
        println("Finish example jpql without fetch.")
    }

    @Test
    fun `example jpql with fetch`() {
        println("Start example jpql with fetch.")
        val room = entityManager.entityManager.createQuery("SELECT r FROM Room r JOIN FETCH r.hotel WHERE r.id = $roomID",Room::class.java).singleResult
        println("Finish example jpql with fetch.")
    }

    @Test
    fun `default fetch behaviour of jpa api`() {
        println("Start default fetch behaviour example.")
        val room = entityManager.entityManager.find(Room::class.java,roomID)
        println("Finish default fetch behaviour example.")
    }

    @AfterTransaction
    fun afterTransaction(){
        roomRepository.deleteAll()
        roomAttributeRepository.deleteAll()
        hotelRepository.deleteAll()
    }
}