package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.dto.ScrapedData
import iee.ihu.gr.pricemonitoringbackend.dto.ScrappingRequest

interface ScrappingService {

    /**
     * Sends scrapping request for hotel identified by provided url and returns hotel info received as response of scrapping if reply doesn't time out.
     * @param scrappingRequest - hotel which should be scrapped.
     * @return true if scrapper handled the request within predefined timeout.
     */
    fun requestHotelInfo(scrappingRequest: ScrappingRequest) : ScrapedData

}