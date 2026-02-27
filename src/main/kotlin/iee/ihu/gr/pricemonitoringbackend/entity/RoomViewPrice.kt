package iee.ihu.gr.pricemonitoringbackend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name ="prices")
@Entity
class RoomViewPrice(
    @Column(name = "quantity", nullable = false)
    var quantity: Int,

    @Column(name = "price", nullable = false)
    var price: Int,

    @Column(name = "distance_days", nullable = false)
    var distanceDays: Int,

    @Column(name = "cancellation_days", nullable = false)
    var cancellationDays: Int,

    @JoinColumn(name = "room_choice_id")
    @ManyToOne(optional = false)
    var roomView: RoomView,

    var timestamp: LocalDateTime,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
)