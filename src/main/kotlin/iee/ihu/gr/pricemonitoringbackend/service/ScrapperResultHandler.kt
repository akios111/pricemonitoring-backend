package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.dto.ScrapperResult

interface ScrapperResultHandler {

    fun onNewHotelInfo(scrappedResult: ScrapperResult)

}