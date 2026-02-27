package iee.ihu.gr.pricemonitoringbackend.service.attributes

import iee.ihu.gr.pricemonitoringbackend.entity.RoomViewAttribute
import iee.ihu.gr.pricemonitoringbackend.repository.RoomViewAttributeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RoomViewAttributeServiceImpl(private val roomViewAttributeRepository: RoomViewAttributeRepository) : RoomViewAttributeService {

    @Transactional(readOnly = true)
    override fun findRoomViewAttributes(attributes: Collection<String>): Collection<RoomViewAttribute> = roomViewAttributeRepository.findByNameIn(attributes)

    override fun createRoomViewAttribute(attribute: String): RoomViewAttribute = roomViewAttributeRepository.save(RoomViewAttribute(attribute))
}