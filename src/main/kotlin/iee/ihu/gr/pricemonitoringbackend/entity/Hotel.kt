package iee.ihu.gr.pricemonitoringbackend.entity

import iee.ihu.gr.pricemonitoringbackend.HOTELS_URL_UNIQUE_CONSTRAINT
import iee.ihu.gr.pricemonitoringbackend.HOTEL_SEEDS_UNIQUE_HOTEL_ID_CONSTRAIN
import org.hibernate.annotations.Check
import java.math.BigDecimal
import java.net.URL
import jakarta.persistence.*

@Check(constraints = "hotel_score IS NULL OR (hotel_score >= 0 AND hotel_score <= 10.0)")
@Entity
@Table(name = "hotels", uniqueConstraints = [UniqueConstraint(name = HOTELS_URL_UNIQUE_CONSTRAINT, columnNames = ["hotel_url"])])
class Hotel(
    @Column(nullable = false, name = "hotel_url")
    var url: URL,

    @Column(name = "hotel_name", nullable = false)
    var name: String,

    @Column(name = "hotel_description", length = 1000, nullable = false)
    var description: String,

    @AttributeOverrides(
        AttributeOverride(name = "address", column = Column(nullable = false)),
        AttributeOverride(name = "city", column = Column(nullable = false)),
        AttributeOverride(name = "longitude", column = Column(nullable = true)),
        AttributeOverride(name = "latitude", column = Column(nullable = true))
    )
    @Column
    var location: Location,

    @Enumerated(EnumType.STRING)
    @Column(name = "hotel_type", nullable = false)
    var hotelType: HotelType,

    @Column(name = "hotel_score", scale = 2, precision = 4, nullable = false)
    var score: BigDecimal,


    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
)