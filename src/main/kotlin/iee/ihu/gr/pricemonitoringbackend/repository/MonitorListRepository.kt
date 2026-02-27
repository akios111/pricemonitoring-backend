package iee.ihu.gr.pricemonitoringbackend.repository

import iee.ihu.gr.pricemonitoringbackend.entity.MonitorList
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

interface MonitorListRepository : JpaRepository<MonitorList,Long>{

    fun findAllByOwner_Id(ownerID: Long) : List<MonitorList>

    @Query("SELECT m FROM MonitorList m LEFT JOIN FETCH m.rooms r LEFT JOIN FETCH r.attributes JOIN FETCH m.owner JOIN FETCH m.distances JOIN FETCH r.hotel WHERE m.id = :id")
    override fun findById(id: Long) : Optional<MonitorList>

    @Query("SELECT COUNT(*) FROM MonitorList m WHERE m.owner.id = :userID")
    fun countUserMonitorList(userID: Long) : Int

    @Query("SELECT COUNT(*) = 1 FROM MonitorList ml WHERE ml.id = :monitorListID AND ml.owner.id = :customerID ")
    fun isOwner(monitorListID: Long,customerID: Long) : Boolean
}