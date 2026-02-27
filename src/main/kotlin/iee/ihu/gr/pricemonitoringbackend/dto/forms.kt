package iee.ihu.gr.pricemonitoringbackend.dto

import iee.ihu.gr.pricemonitoringbackend.BigDecimalSerializer
import iee.ihu.gr.pricemonitoringbackend.entity.*
import iee.ihu.gr.pricemonitoringbackend.toEnumHotelType
import kotlinx.serialization.Serializable
import org.hibernate.validator.constraints.URL
import java.math.BigDecimal
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import kotlinx.serialization.Transient
import java.time.Instant
import java.time.LocalDateTime
import java.util.SortedSet
import java.util.TreeSet

@Serializable
data class CustomerCredentials(val email: String,val password: String)

@Serializable
data class EmailData(@field:jakarta.validation.constraints.Email(message = "{CustomerForm.email.Email.message}") val email: Email)

@Serializable
@EqualPasswords(message = "{CustomerForm.EqualPasswords.message}")
data class CustomerRegistrationForm(

    val jwt: String,

    @field:NotBlank(message = "{CustomerForm.name.NotBlank.message}") val name: String,

    @field:Pattern(regexp = "^(?=.*\\d)(?=.*[a-zA-Z]).{8,}\$", message = "{CustomerForm.password.Patter.message}") val password: String,

    val repeatedPassword: String
)

@Serializable
@EqualPasswords(message = "{CustomerForm.EqualPasswords.message}")
data class PasswordResetForm(
    val jwt: String,

    @field:Pattern(regexp = "^(?=.*\\d)(?=.*[a-zA-Z]).{8,}\$", message = "{CustomerForm.password.Patter.message}") val password: String,

    val repeatedPassword: String
)


@Serializable
data class HotelInfo(
    val name: String,

    val description: String,

    val location: Location,

    val url: String,

    val hotelType: HotelType,

    @Serializable(with = BigDecimalSerializer::class)
    val score: BigDecimal,

    val hotelID: Long,

    val rooms: List<RoomInfo>
)

@Serializable
data class RoomInfo(
    val roomID: Long,

    val description: String,

    val name: String,

    val hotelID: Long,

    val hotelName: String,

    val statistics: RoomStatistics? = null,

    val attributes: List<String>
)

@Serializable
data class ScrapperResult(
    val taskId: String,

    val payload: Payload,

    val data: ScrapedData
)

@Serializable
data class Payload(
    val link: String,
    val dayDistance: Int,
    val retryCount: Int? = null
)


@Serializable
data class ScrapedData(

    val name: String,

    val description: String,

    val location: Location,

    val url: String,

    val hotelType: String,

    @Serializable(with = BigDecimalSerializer::class)
    val score: BigDecimal,

    val rooms: Set<ScrappedRoomInfo> = emptySet()
){

    @kotlinx.serialization.Transient
    val hotelData: HotelData = HotelData(name,description,location,url, hotelType.toEnumHotelType(),score)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScrapedData

        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }
}

data class HotelData(
    @field:NotBlank(message = "{HotelData.name.NotBlank.message}") val name: String,

    @field:NotBlank(message = "{HotelData.description.NotBlank.message}") val description: String,

    @field:Valid val location: Location,

    @field:URL(message = "{HotelData.url.URL.message}", protocol = "https") val url: String,

    val hotelType: HotelType,

    @field:DecimalMin(value = "0.0", message = "{HotelData.score.DecimalMin.message}")
    @field:DecimalMax(value = "10.0", message = "{HotelData.score.DecimalMax.message}")
    @Serializable(with = BigDecimalSerializer::class)
    val score: BigDecimal
)

@Serializable
data class ScrappedRoomInfo(
    @field:NotBlank(message = "{RoomInfo.description.NotBlank.message}")
    val description: String,

    @field:NotBlank(message = "{RoomInfo.name.NotBlank.message}")
    val name: String,

    val roomViews: Set<ScrappedRoomViewInfo> = emptySet(),

    val attributes: Set<String> = emptySet()
)

data class RoomData(
    val hotel: Hotel,

    val description: String,

    @field:NotBlank(message = "{RoomData.name.NotBlank.message}")
    val name: String,

    val attributes: List<RoomAttribute> = emptyList()
)

data class PriceInfo(
    val id: Long,

    val sleeps: Int,

    val price: Int,

    val quantity: Int,

    val distanceDays: Int,

    val cancellationPolicy: CancellationPolicy,

    val timestamp: LocalDateTime,

    val breakfastPolicy: String,

    val attributes: Set<String>
)

@Serializable
data class ScrappedRoomViewInfo(
    @field:Positive(message = "{RoomViewInfo.sleeps.Positive.message}")
    val sleeps: Int,

    @field:Positive(message = "{RoomViewInfo.price.Positive.message}")
    val price: Double,

    @field:PositiveOrZero(message = "{RoomViewInfo.quantity.PositiveOrZero.message}")
    val quantity: Int,

    @field:Positive(message = "{RoomViewInfo.distanceDays.Positive.message}")
    val distanceInDays: Int,

    val attributes: Set<String> = TreeSet()
)

data class RoomViewData(
    @field:Positive(message = "{RoomViewData.sleeps.Positive.message}")
    val sleeps: Int,

    @field:Positive(message = "{RoomViewData.price.Positive.message}")
    val price: Int,

    @field:PositiveOrZero(message = "{RoomViewData.quantity.PositiveOrZero.message}")
    val quantity: Int,

    @field:Positive(message = "{RoomViewData.distanceDays.Positive.message}")
    val distanceDays: Int,

    val room: Room,

    val attributes: SortedSet<RoomViewAttribute> = sortedSetOf()
)

@Serializable
data class HotelInfoRequest(
    @field:Pattern(regexp = ".*\\.html.*", message = "{HotelInfoRequest.link.Pattern.message}")
    @field:URL(protocol = "https", message = "{HotelInfoRequest.link.URL.message}")
    val link: String
)

@Serializable
data class ScrappingRequest(val link: String,val dayDistance: Int,val stayingDays: Int){



}

@Serializable
data class MonitorListCreateForm(
    @field:NotEmpty(message = "{MonitorList.rooms.NotEmpty.message}")
    val rooms: Set<Long>,

    @field:NotBlank(message = "{MonitorList.name.NotBlank.message}")
    val name: String,

    @field:NotEmpty(message = "{MonitorList.distanceDays.NotEmpty.message}")
    val distanceDays: Set<Int>
)

@Serializable
data class MonitorListUpdateForm(
    @field:NotEmpty(message = "{MonitorList.rooms.NotEmpty.message}")
    val rooms: Set<Long>,

    @field:NotBlank(message = "{MonitorList.name.NotBlank.message}")
    val name: String,

    @field:NotEmpty(message = "{MonitorList.distanceDays.NotEmpty.message}")
    val distanceDays: Set<Int>,

    val id: Long
)

@Serializable
data class MonitorListInfo(val monitorListName: String,val monitorListID: Long)

@Serializable
data class MonitorListDetails(val monitorListName: String,val monitorListID: Long,val distances: Set<Int>,val rooms: List<RoomInfo>,val monitorListStatistics: MonitorListStatistics?)

data class RoomChange(val addedAttributes: List<String>,val removedAttributes: List<String>,val timestamp: Long)

data class Mail(val recipient: Email,val subject: String,val body: String)

data class AllRoomStatistics(
    val sum: Long ,

    val count: Long,

    val min: Int,

    val max: Int,

    val roomID: Long
){
    val avg: Long = sum/count
}

@Serializable
data class RoomStatistics(
    val min: Int,

    val max: Int,

    val avg: Long
)

@Serializable
data class MonitorListStatistics(val avg: Long, val min: Int,val max: Int)