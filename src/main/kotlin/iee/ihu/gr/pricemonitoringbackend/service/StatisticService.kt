package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.dto.AllRoomStatistics
import iee.ihu.gr.pricemonitoringbackend.dto.MonitorListStatistics
import iee.ihu.gr.pricemonitoringbackend.dto.RoomStatistics
import iee.ihu.gr.pricemonitoringbackend.entity.MonitorList
import java.time.LocalDate

interface StatisticService {

    fun roomStatistics(monitorList: MonitorList, from: LocalDate?, until: LocalDate?, excludeRooms: List<Long>?) : Map<Long,AllRoomStatistics>

}