package iee.ihu.gr.pricemonitoringbackend.service.attributes

import iee.ihu.gr.pricemonitoringbackend.entity.RoomViewAttribute

interface RoomViewAttributeService {

    fun findRoomViewAttributes(attributes: Collection<String>) : Collection<RoomViewAttribute>

    fun createRoomViewAttribute(attribute: String) : RoomViewAttribute

}