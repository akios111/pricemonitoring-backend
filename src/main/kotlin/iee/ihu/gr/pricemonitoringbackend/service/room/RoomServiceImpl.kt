package iee.ihu.gr.pricemonitoringbackend.service.room

import iee.ihu.gr.pricemonitoringbackend.diff
import iee.ihu.gr.pricemonitoringbackend.dto.RoomChange
import iee.ihu.gr.pricemonitoringbackend.dto.RoomData
import iee.ihu.gr.pricemonitoringbackend.entity.Room
import iee.ihu.gr.pricemonitoringbackend.entity.RoomAttribute
import iee.ihu.gr.pricemonitoringbackend.repository.RoomRepository
import iee.ihu.gr.pricemonitoringbackend.service.StatisticService
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RoomServiceImpl(private val roomRepository: RoomRepository,private val statisticService: StatisticService) : RoomService {

    override fun createRoom(roomData: RoomData) : Room{
        return roomRepository.save(Room(roomData.hotel,roomData.description,roomData.name, mutableSetOf<RoomAttribute>().apply { addAll(roomData.attributes) }))
    }

    override fun updateRoom(roomData: RoomData): Room {
        return roomRepository
            .findByHotelAndName(roomData.hotel,roomData.name)
            .orElseThrow { EmptyResultDataAccessException(1) }
            .apply {
                this.description = roomData.description
                val newAttributes = roomData.attributes.filter { attribute -> !this.attributes.contains(attribute) }
                this.attributes.removeIf { attribute -> !roomData.attributes.contains(attribute) }
                this.attributes.addAll(newAttributes)
            }
    }

    override fun changes(id: Long, pageable: Pageable): Page<RoomChange> {
        val revisions = roomRepository.findRevisions(id,pageable)
        val changes = mutableListOf<RoomChange>()

        if(revisions.isEmpty)
            return PageImpl(emptyList(),revisions.pageable,revisions.totalElements)

        val (initialAttributes,_) = emptyList<RoomAttribute>().diff(revisions.content[0].entity.attributes)
        changes.add(RoomChange(initialAttributes.map(RoomAttribute::name), emptyList(),revisions.content[0].metadata.requiredRevisionInstant.epochSecond))

        for (index in 0 until revisions.content.size - 1){
            val current = revisions.content[index]
            val next = revisions.content[index+1]
            val (newAttributes,removedAttributes) = current.entity.attributes.diff(next.entity.attributes)
            changes.add(RoomChange(newAttributes.map(RoomAttribute::name),removedAttributes.map(RoomAttribute::name),next.metadata.requiredRevisionInstant.epochSecond))
        }

        return PageImpl(changes,revisions.pageable,revisions.totalElements)
    }

}