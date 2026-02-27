package iee.ihu.gr.pricemonitoringbackend.controller

import iee.ihu.gr.pricemonitoringbackend.*
import iee.ihu.gr.pricemonitoringbackend.dto.*
import iee.ihu.gr.pricemonitoringbackend.service.ScrapperDataPersistService
import iee.ihu.gr.pricemonitoringbackend.service.ScrappingService
import iee.ihu.gr.pricemonitoringbackend.service.hotel.HotelService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/hotel")
@RestController
class HotelController(private val hotelService: HotelService,private val scrappingService: ScrappingService,private val scrapperDataPersistService: ScrapperDataPersistService) {

    @PostMapping("/info", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun scrapHotel(@RequestBody hotelInfoRequest: HotelInfoRequest): ResponseEntity<HotelInfo> {
        val scrapResult = scrappingService.requestHotelInfo(hotelInfoRequest.toScrappingRequest())

        return ResponseEntity.ok(scrapperDataPersistService.saveScrappingResult(scrapResult))
    }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getHotel(@PathVariable id: Long) : ResponseEntity<HotelInfo> = ResponseEntity.ok(hotelService.getHotelInfo(id))

}