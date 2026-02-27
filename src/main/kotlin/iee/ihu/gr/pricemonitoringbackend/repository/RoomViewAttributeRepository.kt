package iee.ihu.gr.pricemonitoringbackend.repository

import iee.ihu.gr.pricemonitoringbackend.entity.RoomViewAttribute
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
interface RoomViewAttributeRepository : JpaRepository<RoomViewAttribute,Long>{

    fun findByName(name: String) : Optional<RoomViewAttribute>

    fun findByNameIn(name: Collection<String>) : List<RoomViewAttribute>

}