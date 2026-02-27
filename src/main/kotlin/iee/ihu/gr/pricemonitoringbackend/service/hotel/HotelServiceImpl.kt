package iee.ihu.gr.pricemonitoringbackend.service.hotel

import iee.ihu.gr.pricemonitoringbackend.*
import iee.ihu.gr.pricemonitoringbackend.dto.*
import iee.ihu.gr.pricemonitoringbackend.entity.*
import iee.ihu.gr.pricemonitoringbackend.repository.*
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URL

@Service
@Transactional
class HotelServiceImpl(
    private val hotelRepository: HotelRepository,
    private val roomRepository: RoomRepository
) : HotelService {

    @Transactional(readOnly = true)
    override fun getHotelInfo(hotelID: Long): HotelInfo {
        return hotelRepository
            .findById(hotelID)
            .map { hotel -> hotel.toDTO(roomRepository.findByHotel(hotel)) }
            .orElseThrow { EmptyResultDataAccessException(1) }
    }

    override fun createHotel(hotelData: HotelData): Hotel = with(hotelData) {
        return hotelRepository
            .save(Hotel(URL(hotelData.url.clearLanguageInfo().withoutQueryPart()), name, description, location, hotelType, score))
    }

    override fun updateHotel(hotelData: HotelData): Hotel {
        return hotelRepository
            .findByUrl(URL(hotelData.url.clearLanguageInfo().withoutQueryPart()))
            .orElseThrow { EmptyResultDataAccessException(1) }
            .apply {
                name = hotelData.name
                description = hotelData.description
                location = hotelData.location
                hotelType = hotelData.hotelType
                score = hotelData.score
            }
    }

}