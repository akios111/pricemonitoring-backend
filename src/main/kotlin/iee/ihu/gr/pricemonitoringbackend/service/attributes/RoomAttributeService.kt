package iee.ihu.gr.pricemonitoringbackend.service.attributes

import iee.ihu.gr.pricemonitoringbackend.entity.RoomAttribute

interface RoomAttributeService {

    fun findRoomAttributes(attributes: Collection<String>) : Collection<RoomAttribute>

    fun createRoomAttribute(attribute: String) : RoomAttribute


}