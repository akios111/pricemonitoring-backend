package iee.ihu.gr.pricemonitoringbackend.service.monitor

import iee.ihu.gr.pricemonitoringbackend.dto.MonitorListCreateForm
import iee.ihu.gr.pricemonitoringbackend.dto.MonitorListUpdateForm
import iee.ihu.gr.pricemonitoringbackend.entity.MonitorList
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated

@Validated
interface MonitorListService {

    fun createMonitorList(@Valid createForm: MonitorListCreateForm) : MonitorList

    fun monitorLists(userID: Long) : List<MonitorList>

    fun findMonitorList(monitorListID: Long) : MonitorList

    fun delete(monitorListID: Long)

    fun update(@Valid updateForm: MonitorListUpdateForm)

    fun isOwner(monitorListID: Long,customerID: Long) : Boolean
}