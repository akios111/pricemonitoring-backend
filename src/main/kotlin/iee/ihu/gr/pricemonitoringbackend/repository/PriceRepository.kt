package iee.ihu.gr.pricemonitoringbackend.repository

import iee.ihu.gr.pricemonitoringbackend.dto.AllRoomStatistics
import iee.ihu.gr.pricemonitoringbackend.dto.RoomStatistics
import iee.ihu.gr.pricemonitoringbackend.entity.MonitorList
import iee.ihu.gr.pricemonitoringbackend.entity.RoomViewPrice
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface PriceRepository : JpaRepository<RoomViewPrice,Long>{

    @Query("SELECT p FROM RoomViewPrice p JOIN FETCH p.roomView rv LEFT JOIN FETCH rv.attributes WHERE p.id in :ids")
    fun findByIdIn(ids: Collection<Long>) : List<RoomViewPrice>

    @Query("SELECT p.id FROM RoomViewPrice p WHERE p.roomView.room.id = :roomID AND p.distanceDays in (SELECT ml.distances FROM MonitorList ml WHERE ml.id = :monitorListID)", countQuery = "SELECT count(p) FROM RoomViewPrice p WHERE p.roomView.room.id = :roomID AND p.distanceDays in (SELECT ml.distances FROM MonitorList ml WHERE ml.id = :monitorListID)")
    fun roomPrices(roomID: Long,monitorListID: Long, pageable: Pageable) : Page<Long>

    @Query("""
        SELECT new iee.ihu.gr.pricemonitoringbackend.dto.AllRoomStatistics(SUM(p.price),COUNT(p.price),MIN(p.price),MAX(p.price),p.roomView.room.id) 
        FROM RoomViewPrice p 
        WHERE p.roomView.room IN (:#{#monitorList.rooms}) AND (:exclude IS NULL OR p.roomView.room.id NOT IN (:exclude)) AND p.distanceDays IN (:#{#monitorList.distances}) AND (:#{#from} IS NULL OR p.timestamp >= :from) AND (:#{#until} IS NULL OR p.timestamp <= :until)
        GROUP BY p.roomView.room.id 
    """)
    fun roomStatistics(monitorList: MonitorList, from: LocalDateTime?, until: LocalDateTime?, exclude: List<Long>?) : List<AllRoomStatistics>

}