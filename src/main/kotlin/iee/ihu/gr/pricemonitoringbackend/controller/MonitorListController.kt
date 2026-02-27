package iee.ihu.gr.pricemonitoringbackend.controller

import iee.ihu.gr.pricemonitoringbackend.*
import iee.ihu.gr.pricemonitoringbackend.dto.*
import iee.ihu.gr.pricemonitoringbackend.service.StatisticService
import iee.ihu.gr.pricemonitoringbackend.service.monitor.MonitorListService
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RequestMapping("/monitor_list")
@RestController
class MonitorListController(private val monitorListService: MonitorListService,private val messageSource: MessageSource,private val statisticsService: StatisticService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createMonitorList(@RequestBody monitorList: MonitorListCreateForm): ResponseEntity<Unit>{
        try{
            monitorListService.createMonitorList(monitorList)
        }catch (ex: DataIntegrityViolationException){
            if(ex.message?.contains(MONITOR_LISTS_UNIQUE_NAME_PER_OWNER) == false) throw ex
            throw MonitorListDuplicateNameException(monitorList.name)
        }

        return ResponseEntity.ok(Unit)
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun monitorLists() : ResponseEntity<List<MonitorListInfo>>{
        val userID = SecurityContextHolder.getContext().authentication.name.toLongOrNull() ?: throw IllegalStateException("Cannot get id of the authenticated user.")
        val lists = monitorListService.monitorLists(userID).map { MonitorListInfo(it.name,it.id!!) }
        return ResponseEntity.ok(lists)
    }

    @GetMapping("/{id}",produces = [MediaType.APPLICATION_JSON_VALUE])
    fun monitorListDetails(
        @PathVariable id: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) until: LocalDate?,
        @RequestParam("exclude") excludeRooms: List<Long>?
    ) : ResponseEntity<MonitorListDetails>{
        val userID = SecurityContextHolder.getContext().authentication.name.toLongOrNull() ?: throw IllegalStateException("Cannot get id of the authenticated user.")
        val monitorList = monitorListService.findMonitorList(id)

        if(monitorList.owner.id != userID) throw AccessDeniedException("Access for monitor list with id $id denied.")

        val statistics = statisticsService.roomStatistics(monitorList,from,until,excludeRooms)
        val monitorListStatistics = statistics.values.monitorListStatistics()

        return with(monitorList){
            ResponseEntity.ok(MonitorListDetails(name, id,distances,rooms.map { room -> room.toDTO(statistics[room.id]?.toRoomStatistics()) },monitorListStatistics))
        }
    }

    @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateMonitorList(@RequestBody monitorList: MonitorListUpdateForm) : ResponseEntity<Unit>{
        try{
            monitorListService.update(monitorList)
        }catch (ex: DataIntegrityViolationException){
            if(ex.message?.contains(MONITOR_LISTS_UNIQUE_NAME_PER_OWNER) == false) throw ex
            throw MonitorListDuplicateNameException(monitorList.name)
        }

        return ResponseEntity.ok(Unit)
    }


    @DeleteMapping("/{id}")
    fun deleteMonitorList(@PathVariable id: Long) : ResponseEntity<Unit>{
        monitorListService.delete(id)

        return ResponseEntity.ok(Unit)
    }

    @ExceptionHandler
    fun duplicateMonitorListNameExceptionHandler(ex: MonitorListDuplicateNameException) : ResponseEntity<String>{
        return ResponseEntity.status(HttpStatus.CONFLICT).body(messageSource.getMessage("Monitor.List.Unique.Name.Per.Owner.message", arrayOf(ex.monitorListName),LocaleContextHolder.getLocale()))
    }

}