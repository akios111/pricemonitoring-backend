package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.entity.RoomViewPrice
import iee.ihu.gr.pricemonitoringbackend.repository.PriceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PriceServiceImpl(private val priceRepository: PriceRepository) : PriceService {

    @Transactional(readOnly = true)
    override fun roomPrices(roomID: Long, pageable: Pageable,monitorListID: Long): Page<RoomViewPrice> {
        val ids = priceRepository.roomPrices(roomID,monitorListID,pageable)
        val prices = priceRepository.findByIdIn(ids.content).associateBy { it.id!! }

        return ids.map { id -> prices[id]!! }
    }

}