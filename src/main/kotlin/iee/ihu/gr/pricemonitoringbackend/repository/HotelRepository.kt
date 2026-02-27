package iee.ihu.gr.pricemonitoringbackend.repository

import iee.ihu.gr.pricemonitoringbackend.entity.Hotel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.net.URL
import java.util.Optional
import jakarta.persistence.LockModeType
import jakarta.persistence.PersistenceException
import jakarta.validation.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

interface HotelRepository : JpaRepository<Hotel,Long> {

    fun findByUrl(url: URL) : Optional<Hotel>


}