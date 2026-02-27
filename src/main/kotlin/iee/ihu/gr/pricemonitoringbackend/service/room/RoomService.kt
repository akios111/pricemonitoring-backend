package iee.ihu.gr.pricemonitoringbackend.service.room

import iee.ihu.gr.pricemonitoringbackend.dto.RoomChange
import iee.ihu.gr.pricemonitoringbackend.dto.RoomData
import iee.ihu.gr.pricemonitoringbackend.dto.RoomInfo
import iee.ihu.gr.pricemonitoringbackend.entity.Hotel
import iee.ihu.gr.pricemonitoringbackend.entity.Room
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.validation.annotation.Validated

@Validated
interface RoomService {

    fun createRoom(@Valid roomData:RoomData) : Room

    fun updateRoom(@Valid roomData: RoomData) : Room

    fun changes(id: Long,pageable: Pageable) : Page<RoomChange>

}