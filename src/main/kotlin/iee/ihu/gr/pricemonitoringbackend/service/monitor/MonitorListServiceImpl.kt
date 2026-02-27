package iee.ihu.gr.pricemonitoringbackend.service.monitor

import iee.ihu.gr.pricemonitoringbackend.dto.MonitorListCreateForm
import iee.ihu.gr.pricemonitoringbackend.dto.MonitorListUpdateForm
import iee.ihu.gr.pricemonitoringbackend.entity.MonitorList
import iee.ihu.gr.pricemonitoringbackend.entity.Role
import iee.ihu.gr.pricemonitoringbackend.repository.*
import iee.ihu.gr.pricemonitoringbackend.service.subscription.SubscriptionService
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MonitorListServiceImpl(
    private val monitorListRepository: MonitorListRepository,
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository,
    private val subscriptionService: SubscriptionService
) : MonitorListService {

    override fun createMonitorList(createForm: MonitorListCreateForm) : MonitorList {
        val owner = userRepository
            .findByIdAndLock(SecurityContextHolder.getContext().authentication.name.toLong())
            .orElseThrow { throw IllegalStateException("Unauthorized access in MonitorListService or use of access token of user that has been deleted.") }

        val subscriptionLimit = subscriptionService.subscriptionLimits(owner.role)

        if(subscriptionLimit.maxRooms.map { maxRooms -> createForm.rooms.size > maxRooms }.orElse(false))
            throw IllegalArgumentException("Maximum rooms allowed: ${subscriptionLimit.maxRooms.get()} but form has ${createForm.rooms.size} rooms.")

        if(monitorListRepository.countUserMonitorList(owner.id!!) >= subscriptionLimit.maxMonitorList)
            throw AccessDeniedException("")

        val rooms = roomRepository.findAllById(createForm.rooms)

        if(rooms.size != createForm.rooms.size)
            throw IncorrectResultSizeDataAccessException(createForm.rooms.size,rooms.size)

        return monitorListRepository.save(MonitorList(rooms.toMutableSet(), createForm.name, owner, mutableSetOf<Int>().apply { addAll(createForm.distanceDays) }))
    }

    @Transactional(readOnly = true)
    override fun monitorLists(userID: Long): List<MonitorList> = monitorListRepository.findAllByOwner_Id(userID)

    @Transactional
    override fun findMonitorList(monitorListID: Long): MonitorList {
        return monitorListRepository.findById(monitorListID).orElseThrow { EmptyResultDataAccessException("Monitor list with id $monitorListID not found.", 1) }
    }
    override fun delete(monitorListID: Long) {
        val monitorList = getMonitorListByIdAndCheckOwnership(monitorListID)

        monitorListRepository.delete(monitorList)
    }

    override fun update(updateForm: MonitorListUpdateForm) {
        val role = SecurityContextHolder.getContext().authentication.authorities.map(GrantedAuthority::getAuthority).map { Role.valueOf(it.removePrefix("ROLE_")) }.first()

        val monitorList = getMonitorListByIdAndCheckOwnership(updateForm.id)

        val subscriptionLimit = subscriptionService.subscriptionLimits(role)

        if(subscriptionLimit.maxRooms.map { maxRooms -> updateForm.rooms.size > maxRooms }.orElse(false))
            throw IllegalArgumentException("Maximum rooms allowed: ${subscriptionLimit.maxRooms.get()} but form has ${updateForm.rooms.size} rooms.")

        val rooms = roomRepository.findAllById(updateForm.rooms)

        if(rooms.size != updateForm.rooms.size)
            throw IncorrectResultSizeDataAccessException(updateForm.rooms.size,rooms.size)

        monitorList.name = updateForm.name
        monitorList.rooms.removeIf { room -> !updateForm.rooms.contains(room.id) }
        monitorList.rooms.addAll(rooms)
        monitorList.distances.removeIf { distanceDay -> !updateForm.distanceDays.contains(distanceDay) }
        monitorList.distances.addAll(updateForm.distanceDays)
    }

    override fun isOwner(monitorListID: Long, customerID: Long): Boolean = monitorListRepository.isOwner(monitorListID,customerID)

    private fun getMonitorListByIdAndCheckOwnership(monitorListID: Long) : MonitorList{
        val monitorList = monitorListRepository.findById(monitorListID).orElseThrow { EmptyResultDataAccessException(1) }

        if(monitorList.owner.id != SecurityContextHolder.getContext().authentication.name.toLong())
            throw AccessDeniedException("User trying to delete monitor list that they are not owner of.")

        return monitorList
    }

}