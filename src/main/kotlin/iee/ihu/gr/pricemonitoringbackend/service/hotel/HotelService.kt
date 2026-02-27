package iee.ihu.gr.pricemonitoringbackend.service.hotel

import iee.ihu.gr.pricemonitoringbackend.dto.*
import iee.ihu.gr.pricemonitoringbackend.entity.Hotel
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated

@Validated
interface HotelService {

    /**
     * Returns hotel info of the hotel identified by the provided hotel id or empty Optional if hotel info not found.
     * @param hotelID id of the hotel.
     */
    fun getHotelInfo(hotelID: Long) : HotelInfo

    /**
     * Creates new hotel with provided data.
     * @param hotelData information about new hotel entity.
     * @throws org.springframework.dao.DataIntegrityViolationException when hotel already exists.
     * @return created hotel entity.
     */
    fun createHotel(@Valid hotelData: HotelData) : Hotel

    /**
     * Updates hotel with new data.
     * Note: does not update hotel rooms,only hotel's information.
     * @param hotelData information about the hotel that is used both for identification and update.
     * @return updated hotel entity.
     */
    fun updateHotel(@Valid hotelData: HotelData) : Hotel

}