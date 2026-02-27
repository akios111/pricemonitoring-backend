package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.dto.AllRoomStatistics
import iee.ihu.gr.pricemonitoringbackend.dto.MonitorListStatistics
import iee.ihu.gr.pricemonitoringbackend.dto.RoomStatistics
import iee.ihu.gr.pricemonitoringbackend.entity.MonitorList
import iee.ihu.gr.pricemonitoringbackend.repository.PriceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Transactional
@Service
class StatisticServiceImpl(private val priceRepository: PriceRepository) : StatisticService {

    override fun roomStatistics(monitorList: MonitorList, from: LocalDate?, until: LocalDate?, excludeRooms: List<Long>?): Map<Long, AllRoomStatistics> =
        priceRepository.roomStatistics(monitorList,from?.atTime(0,0,0),until?.atTime(0,0,0),excludeRooms).associateBy { it.roomID }

}