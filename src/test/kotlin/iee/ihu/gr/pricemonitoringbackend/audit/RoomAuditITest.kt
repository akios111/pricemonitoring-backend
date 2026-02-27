package iee.ihu.gr.pricemonitoringbackend.audit

import iee.ihu.gr.pricemonitoringbackend.entity.*
import iee.ihu.gr.pricemonitoringbackend.service.AbstractITest
import iee.ihu.gr.pricemonitoringbackend.service.room.RoomService
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.history.RevisionMetadata
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.net.URL

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class RoomAuditITest(private val roomService: RoomService) : AbstractITest() {

    private lateinit var hotel: Hotel

    private val testAttributes = mutableListOf<RoomAttribute>()

    private val pageRequest = PageRequest.of(0,10)

    @BeforeEach
    fun setUp(){
        hotel = hotelRepository.save(Hotel(URL("http://test_hotel.html"),"Test Hotel","Test Description",Location("Test Address","Test City"), HotelType.HOTEL, BigDecimal.ONE))
        testAttributes.addAll(roomAttributeRepository.saveAll(listOf(RoomAttribute("TEST_ATTR_1"),RoomAttribute("TEST_ATTR_2"),RoomAttribute("TEST_ATTR_3"))))
    }

    @AfterEach
    fun clean(){
        cleanDB()
    }

    @Test
    fun `should create audit row for newly inserted room`() {
        val room = roomRepository.save(Room(hotel,"ANY_DESC","ANY_NAME", mutableSetOf<RoomAttribute>().apply { addAll(testAttributes) }))

        val revisions = roomRepository.findRevisions(room.id!!)

        revisions.content.shouldHaveSize(1)
        assertSoftly(revisions.content[0]) {
            entity shouldBe room
            metadata.revisionType shouldBe RevisionMetadata.RevisionType.INSERT
        }
    }

    @Test
    fun `should insert new audit row when inserting new room's attributes`() {
        val room = roomRepository.save(Room(hotel,"ANY_DESC","ANY_NAME", mutableSetOf<RoomAttribute>().apply { addAll(testAttributes) }))
        val newAttribute = roomAttributeRepository.save(RoomAttribute("NEW_ATTR_1"))

        roomRepository.save(Room(room.hotel,room.description,room.name,room.attributes.apply { add(newAttribute) },room.id!!))

        //execute in transaction to avoid lazy loading exception
        transactionalTemplate.execute {
            val revisions = roomRepository.findRevisions(room.id!!)
            revisions.content.shouldHaveSize(2)
            assertSoftly(revisions.content[1]) {
                entity shouldBe room
                entity.attributes.shouldContainExactly(roomRepository.findById(room.id!!).get().attributes)
                metadata.revisionType shouldBe RevisionMetadata.RevisionType.UPDATE
            }
        }
    }

    @Test
    fun `should insert new audit row when deleting room attribute`() {
        val room = roomRepository.save(Room(hotel,"ANY_DESC","ANY_NAME", mutableSetOf<RoomAttribute>().apply { addAll(testAttributes) }))
        val toBeRemovedAttribute = testAttributes[0]

        roomRepository.save(Room(room.hotel,room.description,room.name,room.attributes.apply { remove(toBeRemovedAttribute) },room.id!!))

        transactionalTemplate.execute{
            val revisions = roomRepository.findRevisions(room.id!!)
            revisions.content.shouldHaveSize(2)
            assertSoftly(revisions.content[1]) {
                entity shouldBe room
                entity.attributes.shouldContainExactly(roomRepository.findById(room.id!!).get().attributes)
                metadata.revisionType shouldBe RevisionMetadata.RevisionType.UPDATE
            }
        }
    }

    @Test
    fun `should insert new audit row when deleting room`() {
        val room = roomRepository.save(Room(hotel,"ANY_DESC","ANY_NAME", mutableSetOf<RoomAttribute>().apply { addAll(testAttributes) }))

        roomRepository.delete(room)

        val revisions = roomRepository.findRevisions(room.id!!)
        assertSoftly(revisions.content[1]) {
            entity shouldBe room
            metadata.revisionType shouldBe RevisionMetadata.RevisionType.DELETE
        }
    }

    @Test
    fun `should not insert new audit row when updating room's name`() {
        val room = roomRepository.save(Room(hotel,"ANY_DESC","ANY_NAME", mutableSetOf<RoomAttribute>().apply { addAll(testAttributes) }))

        roomRepository.save(Room(room.hotel,room.description,"NEW_NAME",room.attributes,room.id!!))

        val revisions = roomRepository.findRevisions(room.id!!)
        revisions.content.shouldHaveSize(1)
        assertSoftly(revisions.content[0]) {
            metadata.revisionType shouldBe RevisionMetadata.RevisionType.INSERT
        }
    }

    @Test
    fun `should not insert new audit row when updating room's description`() {
        val room = roomRepository.save(Room(hotel,"ANY_DESC","ANY_NAME", mutableSetOf<RoomAttribute>().apply { addAll(testAttributes) }))

        roomRepository.save(Room(room.hotel,"NEW_DESCRIPTION",room.name,room.attributes,room.id!!))

        val revisions = roomRepository.findRevisions(room.id!!)
        revisions.content.shouldHaveSize(1)
        assertSoftly(revisions.content[0]) {
            metadata.revisionType shouldBe RevisionMetadata.RevisionType.INSERT
        }
    }

    @Test
    fun `should return empty page when requesting attribute changes of non existing room`() {
        val idOfNonExistingRoom = 2013910L
        roomRepository.existsById(idOfNonExistingRoom).shouldBeFalse()

        roomService.changes(idOfNonExistingRoom, pageRequest).isEmpty.shouldBeTrue()
    }

    @Test
    fun `should return page with single change without any attribute changes when requesting changes of newly inserted room that has no attributes`() {
        val room = roomRepository.save(Room(hotel,"ANY_DESC","ANY_NAME"))

        val result = roomService.changes(room.id!!,pageRequest)

        result.content.shouldHaveSize(1)
        result.content[0].addedAttributes.shouldBeEmpty()
        result.content[0].removedAttributes.shouldBeEmpty()
    }

    @Test
    fun `should return page with single change that contains all the attributes of a newly inserted room`() {
        val room = roomRepository.save(Room(hotel,"ANY_DESC","ANY_NAME", mutableSetOf<RoomAttribute>().apply { addAll(testAttributes) }))

        val result = roomService.changes(room.id!!,pageRequest)

        result.content.shouldHaveSize(1)
        result.content[0].addedAttributes.shouldContainExactlyInAnyOrder(testAttributes.map(RoomAttribute::name))
        result.content[0].removedAttributes.shouldBeEmpty()
    }

    @Test
    fun `should return page with changes of room attributes`() {
        val room = roomRepository.save(Room(hotel,"ANY_DESC","ANY_NAME", mutableSetOf<RoomAttribute>().apply { addAll(testAttributes) }))
        //remove an attribute from room attributes
        transactionalTemplate.execute { roomRepository.findById(room.id!!).get().attributes.remove(testAttributes[0]) }

        val result = roomService.changes(room.id!!,pageRequest)

        result.content.shouldHaveSize(2)
        result.content[0].addedAttributes.shouldContainExactlyInAnyOrder(testAttributes.map(RoomAttribute::name))
        result.content[0].removedAttributes.shouldBeEmpty()

        result.content[1].addedAttributes.shouldBeEmpty()
        result.content[1].removedAttributes.shouldHaveSize(1).shouldContain(testAttributes[0].name)
    }

    @Test
    fun `should return page with changes of room attributes(2)`() {
        val room = roomRepository.save(Room(hotel,"ANY_DESC","ANY_NAME", mutableSetOf<RoomAttribute>().apply { addAll(testAttributes) }))
        val newAttribute = roomAttributeRepository.save(RoomAttribute("SOME_NEW_ATTR"))
        //remove an attribute from room attributes
        transactionalTemplate.execute {
            roomRepository.findById(room.id!!).get().apply {
                attributes.remove(testAttributes[0])
                attributes.add(newAttribute)
            }
        }

        val result = roomService.changes(room.id!!,pageRequest)

        result.content.shouldHaveSize(2)
        result.content[0].addedAttributes.shouldContainExactlyInAnyOrder(testAttributes.map(RoomAttribute::name))
        result.content[0].removedAttributes.shouldBeEmpty()

        result.content[1].addedAttributes.shouldHaveSize(1).shouldContainExactly(newAttribute.name)
        result.content[1].removedAttributes.shouldHaveSize(1).shouldContain(testAttributes[0].name)
    }

    @Test
    fun `should return page with changes of room attributes(3)`() {
        val room = roomRepository.save(Room(hotel,"ANY_DESC","ANY_NAME", mutableSetOf<RoomAttribute>().apply { addAll(testAttributes) }))
        //remove an attribute from room attributes
        transactionalTemplate.execute { roomRepository.findById(room.id!!).get().attributes.remove(testAttributes[0]) }
        //add the removed attribute back
        transactionalTemplate.execute { roomRepository.findById(room.id!!).get().attributes.add(testAttributes[0]) }

        val result = roomService.changes(room.id!!,pageRequest)

        result.content.shouldHaveSize(3)
        result.content[0].addedAttributes.shouldContainExactlyInAnyOrder(testAttributes.map(RoomAttribute::name))
        result.content[0].removedAttributes.shouldBeEmpty()

        result.content[1].addedAttributes.shouldBeEmpty()
        result.content[1].removedAttributes.shouldHaveSize(1).shouldContain(testAttributes[0].name)

        result.content[2].addedAttributes.shouldHaveSize(1).shouldContain(testAttributes[0].name)
        result.content[2].removedAttributes.shouldBeEmpty()
    }
}