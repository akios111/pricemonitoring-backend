package iee.ihu.gr.pricemonitoringbackend.service.room

import iee.ihu.gr.pricemonitoringbackend.dto.RoomViewData
import iee.ihu.gr.pricemonitoringbackend.entity.*
import iee.ihu.gr.pricemonitoringbackend.repository.PriceRepository
import iee.ihu.gr.pricemonitoringbackend.repository.RoomViewRepository
import iee.ihu.gr.pricemonitoringbackend.service.BreakfastAttributeExtractor
import iee.ihu.gr.pricemonitoringbackend.service.CancellationDaysExtractor
import iee.ihu.gr.pricemonitoringbackend.service.CancellationPolicyExtractor
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.Period
import java.time.temporal.ChronoUnit

@Service
@Transactional
class RoomViewServiceImpl(
    private val roomViewRepository: RoomViewRepository,
    private val cancellationPolicyExtractor: CancellationPolicyExtractor,
    private val breakfastAttributeExtractor: BreakfastAttributeExtractor,
    private val cancellationDaysExtractor: CancellationDaysExtractor,
    private val priceRepository: PriceRepository
) : RoomViewService {


    override fun createRoomView(roomViewData: RoomViewData): RoomView {
        val timestamp = LocalDateTime.now()
        val (cancellationPolicy,breakfastAttribute,cancellationDays) = roomViewData.getImportantAttributes(timestamp)
        val roomView = roomViewRepository.save(RoomView(LocalDateTime.now(),roomViewData.sleeps,roomViewData.room,cancellationPolicy,breakfastAttribute))
        roomView.attributes.addAll(roomViewData.attributes)
        priceRepository.save(RoomViewPrice(roomViewData.quantity,roomViewData.price,roomViewData.distanceDays,cancellationDays,roomView,timestamp))
        return roomView
    }

    override fun updateRoomView(roomViewData: RoomViewData): RoomView {
        val timestamp = LocalDateTime.now()
        val (cancellationPolicy,breakfastAttribute,cancellationDays) = roomViewData.getImportantAttributes(timestamp)
        val roomView = roomViewRepository
            .findRoomViewBySleepsAndCancellationPolicyAndBreakfastAndRoom(roomViewData.sleeps,cancellationPolicy,breakfastAttribute,roomViewData.room)
            .orElseThrow { EmptyResultDataAccessException(1) }
        val newAttributes = roomViewData.attributes.filter { attribute -> !roomView.attributes.contains(attribute) }

        roomView.attributes.removeIf{ attribute -> !roomViewData.attributes.contains(attribute) }
        roomView.attributes.addAll(newAttributes)
        priceRepository.save(RoomViewPrice(roomViewData.quantity,roomViewData.price,roomViewData.distanceDays,cancellationDays,roomView,timestamp))

        return roomView
    }

    private fun RoomViewData.getImportantAttributes(timestamp: LocalDateTime) : Triple<CancellationPolicy,String,Int>{
        val attributeNames = attributes.map(RoomViewAttribute::name).toSet()
        val cancellationPolicy = cancellationPolicyExtractor.apply(attributeNames) ?: CancellationPolicy.UNKNOWN
        val breakfastAttribute = breakfastAttributeExtractor.apply(attributeNames) ?: "N/A"
        val cancellationDay = if(cancellationPolicy == CancellationPolicy.FREE_CANCELLATION) {
            cancellationDaysExtractor.apply(attributeNames)
        }else{
            null
        }

        val cancellationDays = if(cancellationDay != null){
            ChronoUnit.DAYS.between(timestamp.toLocalDate().plusDays(distanceDays.toLong()),cancellationDay).let { if(it < 0) 0 else it.toInt() }
        }else{
            0
        }

        return Triple(cancellationPolicy,breakfastAttribute,cancellationDays)
    }

}