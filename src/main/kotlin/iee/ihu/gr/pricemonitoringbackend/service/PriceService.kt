package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.entity.RoomViewPrice
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PriceService {

    fun roomPrices(roomID: Long,pageable: Pageable,monitorListID: Long) : Page<RoomViewPrice>

}