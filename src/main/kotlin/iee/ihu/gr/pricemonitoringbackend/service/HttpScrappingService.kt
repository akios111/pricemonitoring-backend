package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.dto.ScrapedData
import iee.ihu.gr.pricemonitoringbackend.dto.ScrappingRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class HttpScrappingService(private val webClient: WebClient,private val json: Json) : ScrappingService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun requestHotelInfo(scrappingRequest: ScrappingRequest): ScrapedData {
        logger.info("Sending request to scrapper for hotel {}",scrappingRequest.link)
        return try{
            val jsonResult = webClient
                .post()
                .uri("/api/get-room-info")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(scrappingRequest)
                .retrieve()
                .bodyToMono(String::class.java)
                .block()!!
            json.decodeFromString(jsonResult)
        }catch (ex: Exception){
            logger.error("Error during scrapping service call",ex)
            throw ex
        }
    }

}