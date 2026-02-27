package iee.ihu.gr.pricemonitoringbackend.audit

import iee.ihu.gr.pricemonitoringbackend.entity.*
import iee.ihu.gr.pricemonitoringbackend.service.AbstractITest
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.history.RevisionMetadata
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.net.URL
import java.time.LocalDateTime

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class RoomViewAuditITest : AbstractITest() {

    private lateinit var room: Room

    private val testAttributes = mutableListOf<RoomViewAttribute>()

    @BeforeEach
    fun setUp() {
        val hotel = hotelRepository.save(Hotel(URL("https://test.html"),"ANY_HOTEL_NAME","ANY_HOTEL_DESCRIPTION",Location("ANY_ADDRESS","ANY_CITY"),HotelType.HOTEL, BigDecimal.TEN))
        room = roomRepository.save(Room(hotel,"ANY_ROOM_DESCRIPTION","ANY_ROOM_NAME"))
        testAttributes.addAll(roomViewAttributeRepository.saveAll(listOf(RoomViewAttribute("ANY_ATTR_1"),RoomViewAttribute("ANY_ATTR_2"),RoomViewAttribute("ANY_ATTR_3"))))
    }

    @AfterEach
    fun tearDown() {
        cleanDB()
    }

    @Test
    fun `should insert new audit row when creating new room view`() {
        val roomView = roomViewRepository.save(RoomView(LocalDateTime.now(),2,room,CancellationPolicy.FREE_CANCELLATION,"", sortedSetOf<RoomViewAttribute>().apply { addAll(testAttributes) }))

        transactionalTemplate.execute{
            val revisions = roomViewRepository.findRevisions(roomView.id!!)
            revisions.content.shouldHaveSize(1)
            assertSoftly(revisions.content[0]) {
                metadata.revisionType shouldBe RevisionMetadata.RevisionType.INSERT
                entity shouldBe roomView
                entity.attributes.shouldNotBeEmpty().shouldContainExactly(roomViewRepository.findById(roomView.id!!).get().attributes)
            }
        }
    }

    @Test
    fun `should insert new audit room row when inserting new attribute`() {
        val roomView = roomViewRepository.save(RoomView(LocalDateTime.now(),2,room,CancellationPolicy.FREE_CANCELLATION,"", sortedSetOf<RoomViewAttribute>().apply { addAll(testAttributes) }))
        val newAttribute = roomViewAttributeRepository.save(RoomViewAttribute("NEW_ATTR_1"))

        roomViewRepository.save(RoomView(roomView.startedOn,roomView.sleeps,roomView.room,roomView.cancellationPolicy,roomView.breakfast,roomView.attributes.apply { add(newAttribute) },roomView.id))

        transactionalTemplate.execute{
            val revisions = roomViewRepository.findRevisions(roomView.id!!)
            revisions.content.shouldHaveSize(2)
            assertSoftly(revisions.content[1]) {
                metadata.revisionType shouldBe RevisionMetadata.RevisionType.UPDATE
                entity shouldBe roomView
                entity.attributes.shouldNotBeEmpty().shouldContainExactly(roomViewRepository.findById(roomView.id!!).get().attributes)
            }
        }
    }

    @Test
    fun `should insert new audit room row when removing attribute`() {
        val roomView = roomViewRepository.save(RoomView(LocalDateTime.now(),2,room,CancellationPolicy.FREE_CANCELLATION,"", sortedSetOf<RoomViewAttribute>().apply { addAll(testAttributes) }))
        val toBeRemovedAttribute = testAttributes[0]

        roomViewRepository.save(RoomView(roomView.startedOn,roomView.sleeps,roomView.room,roomView.cancellationPolicy,roomView.breakfast,roomView.attributes.apply { remove(toBeRemovedAttribute) },roomView.id))

        transactionalTemplate.execute{
            val revisions = roomViewRepository.findRevisions(roomView.id!!)
            revisions.content.shouldHaveSize(2)
            assertSoftly(revisions.content[1]) {
                metadata.revisionType shouldBe RevisionMetadata.RevisionType.UPDATE
                entity shouldBe roomView
                entity.attributes.shouldNotBeEmpty().shouldContainExactly(roomViewRepository.findById(roomView.id!!).get().attributes)
            }
        }
    }

}