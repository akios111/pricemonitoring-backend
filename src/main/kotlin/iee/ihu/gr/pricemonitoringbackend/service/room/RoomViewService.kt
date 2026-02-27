package iee.ihu.gr.pricemonitoringbackend.service.room

import iee.ihu.gr.pricemonitoringbackend.dto.RoomViewData
import iee.ihu.gr.pricemonitoringbackend.entity.RoomView
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated

@Validated
interface RoomViewService {

    fun createRoomView(@Valid roomViewData: RoomViewData) : RoomView

    fun updateRoomView(@Valid roomViewData: RoomViewData): RoomView

}