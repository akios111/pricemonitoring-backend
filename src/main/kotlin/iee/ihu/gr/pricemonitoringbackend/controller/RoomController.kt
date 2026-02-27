package iee.ihu.gr.pricemonitoringbackend.controller

import iee.ihu.gr.pricemonitoringbackend.dto.RoomChange
import iee.ihu.gr.pricemonitoringbackend.service.room.RoomService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/room")
@RestController
class RoomController(private val roomService: RoomService) {

    @GetMapping("/history/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun history(@PathVariable id: Long,pageable: Pageable,assembler: PagedResourcesAssembler<RoomChange>): ResponseEntity<PagedModel<EntityModel<RoomChange>>>{
        return ResponseEntity.ok(assembler.toModel(roomService.changes(id,pageable)))
    }

}