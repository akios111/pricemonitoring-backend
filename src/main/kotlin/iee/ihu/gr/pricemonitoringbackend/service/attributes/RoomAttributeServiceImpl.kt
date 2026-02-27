package iee.ihu.gr.pricemonitoringbackend.service.attributes

import iee.ihu.gr.pricemonitoringbackend.entity.RoomAttribute
import iee.ihu.gr.pricemonitoringbackend.repository.RoomAttributeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RoomAttributeServiceImpl(private val roomAttributeRepository: RoomAttributeRepository) : RoomAttributeService {

    @Transactional(readOnly = true)
    override fun findRoomAttributes(attributes: Collection<String>): Collection<RoomAttribute> = roomAttributeRepository.findByNameIn(attributes)

    override fun createRoomAttribute(attribute: String): RoomAttribute = roomAttributeRepository.save(RoomAttribute(attribute))
}