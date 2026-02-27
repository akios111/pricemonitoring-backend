package iee.ihu.gr.pricemonitoringbackend

import iee.ihu.gr.pricemonitoringbackend.dto.*
import iee.ihu.gr.pricemonitoringbackend.entity.*
import iee.ihu.gr.pricemonitoringbackend.service.attributes.RoomAttributeService
import iee.ihu.gr.pricemonitoringbackend.service.attributes.RoomViewAttributeService
import iee.ihu.gr.pricemonitoringbackend.service.hotel.HotelService
import iee.ihu.gr.pricemonitoringbackend.service.room.RoomService
import iee.ihu.gr.pricemonitoringbackend.service.room.RoomViewService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.dao.DataIntegrityViolationException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.regex.Pattern

fun String.toEnumHotelType(): HotelType = when (this.lowercase()) {
    "apartment" -> HotelType.PRIVATE_HOST
    else -> HotelType.valueOf(uppercase())
}

fun String.withoutQueryPart(): String = substring(0, indexOfOrFull("?"))

fun String.indexOfOrFull(str: String): Int {
    val index = this.indexOf(str)

    return if (index != -1) {
        index
    } else {
        this.lastIndex + 1
    }
}

fun String.lastIndexOfOrFull(str: String): Int {
    val index = this.lastIndexOf(str)

    return if (index != -1) {
        index
    } else {
        this.lastIndex + 1
    }
}

fun String.clearLanguageInfo(): String {
    val queryPartIndex = indexOf("?")
    val queryPart = if (queryPartIndex != -1) {
        substring(queryPartIndex).replace(Regex("lang=.*&"), "lang=en-us&")
    } else {
        ""
    }
    val withoutHtmlPart = substring(0, indexOf(".html"))
    val lastSlashIndex = withoutHtmlPart.lastIndexOf("/")
    val hotelPart = withoutHtmlPart.substring(lastSlashIndex)
    val mainPart = withoutHtmlPart.substring(0, lastSlashIndex)
    val withoutLanguagePart = hotelPart.substring(0, hotelPart.lastIndexOfOrFull("."))

    return mainPart.plus(withoutLanguagePart).plus(".html").plus(queryPart)
}

fun String.urlDecode(): String = URLDecoder.decode(this, StandardCharsets.UTF_8.name())

private val checkInPattern = Pattern.compile("checkin=(\\d{4}-\\d{2}-\\d{2})")
private val checkOutPattern = Pattern.compile("checkout=(\\d{4}-\\d{2}-\\d{2})")

fun HotelInfoRequest.toScrappingRequest(): ScrappingRequest {
    val checkInMatch = checkInPattern.matcher(link)
    val checkOutMatch = checkOutPattern.matcher(link)
    val (distanceDays, stayingDays) = if (checkInMatch.find() && checkOutMatch.find()) {
        val checkIn = LocalDate.parse(checkInMatch.group(1), DateTimeFormatter.ISO_DATE)
        val checkOut = LocalDate.parse(checkOutMatch.group(1), DateTimeFormatter.ISO_DATE)
        ChronoUnit.DAYS.between(LocalDate.now(), checkIn) to ChronoUnit.DAYS.between(checkIn, checkOut)
    } else {
        return ScrappingRequest(link, 7, 1)
    }

    return ScrappingRequest(link, distanceDays.toInt(), stayingDays.toInt())
}

fun Email.mask(): Email {
    val email = StringBuilder(value.length)
    email.append(value[0])
    email.append(value[1])

    for (index in 2 until value.length) {
        email.append("*")
    }
    email.append(value.substring(value.indexOf("@")))

    return Email(email.toString())
}

fun <T> Collection<T>.diff(newCollection: Collection<T>): Pair<Collection<T>, Collection<T>> {
    val newElements = mutableListOf<T>()
    val removedElements = mutableListOf<T>()

    newCollection.forEach { element ->
        if (!contains(element)) {
            newElements.add(element)
        }
    }

    forEach { element ->
        if (!newCollection.contains(element)) {
            removedElements.add(element)
        }
    }

    return newElements to removedElements
}

fun HttpServletRequest.ipAddress(): String {
    var ip = getHeader("X-FORWARDED-FOR");

    if (ip == null) {
        ip = remoteAddr
    }

    return ip
}

fun HotelService.createOrUpdate(hotelData: HotelData): Hotel = try {
    createHotel(hotelData)
} catch (ex: DataIntegrityViolationException) {
    if (ex.message?.contains(HOTELS_URL_UNIQUE_CONSTRAINT) == false) throw ex
    updateHotel(hotelData)
}

fun RoomService.createOrUpdate(roomData: RoomData): Room = try {
    createRoom(roomData)
} catch (ex: DataIntegrityViolationException) {
    if (ex.message?.contains(iee.ihu.gr.pricemonitoringbackend.HOTEL_ROOMS_UNIQUE_NAME_CONSTRAINT) == false) throw ex
    updateRoom(roomData)
}

fun RoomViewService.createOrUpdate(roomViewData: RoomViewData): RoomView = try {
    createRoomView(roomViewData)
} catch (ex: DataIntegrityViolationException) {
    if (ex.message?.contains(HOTEL_ROOM_CHOICE_UNIQUE_CONSTRAINT) == false) throw ex
    updateRoomView(roomViewData)
}

fun RoomViewAttributeService.createOrInsertInto(collection: MutableCollection<String>, attribute: String): RoomViewAttribute? = try {
    createRoomViewAttribute(attribute)
} catch (ex: DataIntegrityViolationException) {
    if (ex.message?.contains(ROOM_CHOICE_ATTRIBUTES_UNIQUE_NAME_CONSTRAINT) == false) throw ex
    collection.add(attribute)
    null
}

fun RoomAttributeService.createOrInsertInto(collection: MutableCollection<String>, attribute: String): RoomAttribute? = try {
    createRoomAttribute(attribute)
} catch (ex: DataIntegrityViolationException) {
    if (ex.message?.contains(ROOM_CHOICE_ATTRIBUTES_UNIQUE_NAME_CONSTRAINT) == false) throw ex
    collection.add(attribute)
    null
}

fun Hotel.toDTO(rooms: List<Room>) : HotelInfo = HotelInfo(name,description,location,url.toString(),hotelType,score,id!!,rooms.map{ room -> room.toDTO() })

fun Room.toDTO(roomStatistics: RoomStatistics? = null) : RoomInfo = RoomInfo(id!!,description,name,hotel.id!!,hotel.name,roomStatistics,attributes.map(RoomAttribute::name))

fun Collection<AllRoomStatistics>.monitorListStatistics() : MonitorListStatistics? {
    val min = this.minOfOrNull(AllRoomStatistics::min) ?: return null
    val max = this.maxOfOrNull(AllRoomStatistics::max) ?: return null
    val sum = this.map(AllRoomStatistics::sum).sum()
    val count = this.map(AllRoomStatistics::count).sum()
    val avg = sum/count

    return MonitorListStatistics(avg,min,max)
}

fun AllRoomStatistics.toRoomStatistics() : RoomStatistics = RoomStatistics(min,max,sum/count)