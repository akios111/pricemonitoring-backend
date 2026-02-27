package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.*
import iee.ihu.gr.pricemonitoringbackend.dto.*
import iee.ihu.gr.pricemonitoringbackend.entity.RoomAttribute
import iee.ihu.gr.pricemonitoringbackend.entity.RoomViewAttribute
import iee.ihu.gr.pricemonitoringbackend.service.attributes.RoomAttributeService
import iee.ihu.gr.pricemonitoringbackend.service.attributes.RoomViewAttributeService
import iee.ihu.gr.pricemonitoringbackend.service.hotel.HotelService
import iee.ihu.gr.pricemonitoringbackend.service.room.RoomService
import iee.ihu.gr.pricemonitoringbackend.service.room.RoomViewService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.SortedSet

@Service
class ScrapperDataPersistService(
    private val hotelService: HotelService,
    private val roomService: RoomService,
    private val roomViewService: RoomViewService,
    private val roomAttributeService: RoomAttributeService,
    private val roomViewAttributeService: RoomViewAttributeService,
    private val statisticService: StatisticService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun saveScrappingResult(data: ScrapedData) : HotelInfo {
        val hotel = hotelService.createOrUpdate(data.hotelData)

        val rooms = data.rooms.map { room ->
            val savedRoom = roomService.createOrUpdate(RoomData(hotel,room.description,room.name,createRoomAttributes(room.attributes)))
            room.roomViews.forEach { roomView ->
                roomViewService.createOrUpdate(RoomViewData(roomView.sleeps,roomView.price.toInt(),roomView.quantity,roomView.distanceInDays,savedRoom,createRoomViewAttributes(roomView.attributes)))
            }
            savedRoom
        }
        return hotel.toDTO(rooms,)
    }

    private fun createRoomViewAttributes(attributes: Set<String>): SortedSet<RoomViewAttribute> {
        val existingAttributes = roomViewAttributeService.findRoomViewAttributes(attributes)
        val existingAttributeNames = existingAttributes.map(RoomViewAttribute::name)
        val fetchAttributes = mutableListOf<String>()

        val savedAttributes = attributes
            .filter { attribute -> !existingAttributeNames.contains(attribute) }
            .mapNotNull { attribute -> roomViewAttributeService.createOrInsertInto(fetchAttributes,attribute) }

        val refetchedAttributes = roomViewAttributeService.findRoomViewAttributes(fetchAttributes)

        logger.debug("Needed to fetch {} room choice attributes and got back {} room choice attributes.",fetchAttributes.size,refetchedAttributes.size)

        return sortedSetOf<RoomViewAttribute>().apply { addAll(existingAttributes.plus(savedAttributes).plus(refetchedAttributes)) }
    }

    private fun createRoomAttributes(attributes: Collection<String>): List<RoomAttribute> {
        val existingAttributes = roomAttributeService.findRoomAttributes(attributes)
        val existingAttributeNames = existingAttributes.map(RoomAttribute::name)
        val fetchAttributes = mutableListOf<String>()

        val savedAttributes = attributes
            .filter { attribute -> !existingAttributeNames.contains(attribute) }
            .mapNotNull { attribute -> roomAttributeService.createOrInsertInto(fetchAttributes,attribute) }

        val refetchedAttributes = roomAttributeService.findRoomAttributes(fetchAttributes)

        if(logger.isDebugEnabled)
            logger.debug("Needed to fetch ${fetchAttributes.size} room attributes and got back ${refetchedAttributes.size} room attributes.")

        return existingAttributes.plus(savedAttributes).plus(refetchedAttributes)
    }

}