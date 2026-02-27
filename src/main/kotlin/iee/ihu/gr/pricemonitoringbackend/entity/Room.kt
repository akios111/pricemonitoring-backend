package iee.ihu.gr.pricemonitoringbackend.entity

import iee.ihu.gr.pricemonitoringbackend.HOTEL_ROOMS_UNIQUE_NAME_CONSTRAINT
import java.net.URL
import jakarta.persistence.*
import org.hibernate.envers.Audited
import org.hibernate.envers.RelationTargetAuditMode

@Entity
@Table(name = "hotel_rooms", uniqueConstraints = [UniqueConstraint(name = HOTEL_ROOMS_UNIQUE_NAME_CONSTRAINT, columnNames = ["hotel_room_name","hotel_id"])])
class Room(
    @JoinColumn(name = "hotel_id")
    @ManyToOne(optional = false)
    var hotel: Hotel,

    @Column(name = "hotel_room_description", nullable = false, length = 10000)
    var description: String,

    @Column(name = "hotel_room_name", nullable = false)
    var name: String,

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JoinTable(name = "hotel_rooms_owned_attributes", joinColumns = [JoinColumn(name = "hotel_room_id")], inverseJoinColumns = [JoinColumn(name = "room_attribute_id")])
    @ManyToMany
    var attributes: MutableSet<RoomAttribute> = mutableSetOf(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Room

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}