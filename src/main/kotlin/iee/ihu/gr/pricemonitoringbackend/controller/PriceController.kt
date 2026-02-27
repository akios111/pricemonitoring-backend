package iee.ihu.gr.pricemonitoringbackend.controller

import iee.ihu.gr.pricemonitoringbackend.dto.PriceInfo
import iee.ihu.gr.pricemonitoringbackend.entity.RoomViewAttribute
import iee.ihu.gr.pricemonitoringbackend.entity.RoomViewPrice
import iee.ihu.gr.pricemonitoringbackend.service.PriceService
import iee.ihu.gr.pricemonitoringbackend.service.monitor.MonitorListService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/prices")
@RestController
class PriceController(private val priceService: PriceService,private val monitorListService: MonitorListService) {

    @GetMapping("/{monitorListID}/{roomID}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun roomPrices(@PathVariable roomID: Long,@PathVariable monitorListID: Long, pageable: Pageable,assembler: PagedResourcesAssembler<PriceInfo>) : ResponseEntity<PagedModel<EntityModel<PriceInfo>>>{
        if(!monitorListService.isOwner(monitorListID,SecurityContextHolder.getContext().authentication.name.toLong()))
            throw AccessDeniedException("")

        val page = priceService.roomPrices(roomID,pageable,monitorListID)

        val mappedContent = page.content.map {
            PriceInfo(it.id!!,it.roomView.sleeps,it.price,it.quantity,it.distanceDays,it.roomView.cancellationPolicy,it.timestamp,it.roomView.breakfast,it.roomView.attributes.map(RoomViewAttribute::name).toSet())
        }
        return ResponseEntity.ok(assembler.toModel(PageImpl(mappedContent,pageable,page.totalElements)))
    }

}