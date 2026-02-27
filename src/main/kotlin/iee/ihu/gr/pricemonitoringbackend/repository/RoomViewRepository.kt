package iee.ihu.gr.pricemonitoringbackend.repository

import iee.ihu.gr.pricemonitoringbackend.entity.CancellationPolicy
import iee.ihu.gr.pricemonitoringbackend.entity.Room
import iee.ihu.gr.pricemonitoringbackend.entity.RoomView
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.history.RevisionRepository
import java.util.Optional
interface RoomViewRepository : JpaRepository<RoomView,Long>, RevisionRepository<RoomView,Long,Int>{

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RoomView r WHERE r.sleeps = :sleeps AND r.cancellationPolicy = :cancellationPolicy AND r.breakfast = :breakfast AND r.room = :room")
    fun findRoomViewBySleepsAndCancellationPolicyAndBreakfastAndRoom(sleeps: Int, cancellationPolicy: CancellationPolicy, breakfast: String, room: Room) : Optional<RoomView>


}