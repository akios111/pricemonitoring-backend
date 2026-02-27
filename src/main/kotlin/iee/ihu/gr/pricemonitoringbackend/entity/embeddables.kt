package iee.ihu.gr.pricemonitoringbackend.entity

import kotlinx.serialization.Serializable
import org.hibernate.annotations.ColumnDefault
import java.time.LocalDateTime
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Serializable
@Embeddable
data class Location(

    @field:NotBlank(message = "{Location.address.NotBlank.message}")
    @field:Size(min = 0, max = 100,message = "{Location.address.Size.message}")
    @Column(nullable = false,length = 100)
    var address: String,

    @field:NotBlank(message = "{Location.city.NotBlank.message}")
    @field:Size(min = 0, max = 30, message = "{Location.city.Size.message}")
    @Column(nullable = false,length = 30)
    var city: String,

    @Column(name = "latitude")
    var longitude: Float? = null,

    @Column(name = "longitude")
    var latitude: Float? = null,

    @Transient
    var postalCode: String? = null,

    @Transient
    var fullAddress: String? = null
)