package iee.ihu.gr.pricemonitoringbackend.repository

import iee.ihu.gr.pricemonitoringbackend.entity.Hotel
import iee.ihu.gr.pricemonitoringbackend.entity.Room
import jakarta.persistence.LockModeType
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
interface RoomRepository : JpaRepository<Room,Long>, RevisionRepository<Room,Long,Int> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByHotelAndName(hotel: Hotel, name: String) : Optional<Room>

    fun findByHotel(hotel: Hotel) : List<Room>

}