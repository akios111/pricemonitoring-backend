package iee.ihu.gr.pricemonitoringbackend.repository

import iee.ihu.gr.pricemonitoringbackend.entity.RoomAttribute
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Repository
interface RoomAttributeRepository : JpaRepository<RoomAttribute,Long> {

    fun findByName(name: String) : Optional<RoomAttribute>

    fun findByNameIn(attributes: Collection<String>) : List<RoomAttribute>

}