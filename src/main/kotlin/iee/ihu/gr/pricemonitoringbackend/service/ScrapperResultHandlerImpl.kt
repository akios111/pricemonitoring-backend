package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.dto.*
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class ScrapperResultHandlerImpl(private val scrappingDataPersistService: ScrapperDataPersistService) : ScrapperResultHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @RabbitListener(id = "scrapper_result_handler",queues = ["\${scrapping.response.queue.name}"])
    override fun onNewHotelInfo(scrappedResult: ScrapperResult) {
        try{
            scrappingDataPersistService.saveScrappingResult(scrappedResult.data)
        }catch (ex: Exception){
            logger.error("Error during scraping result handling for hotel with url {} and provided data {}",scrappedResult.payload.link,scrappedResult.data,ex)
        }
    }

}