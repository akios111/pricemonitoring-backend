package iee.ihu.gr.pricemonitoringbackend.entity

import iee.ihu.gr.pricemonitoringbackend.HOTEL_ROOM_ATTRIBUTES_UNIQUE_NAME_CONSTRAINT
import jakarta.persistence.*

@Table(name = "hotel_room_attributes", uniqueConstraints = [UniqueConstraint(name = HOTEL_ROOM_ATTRIBUTES_UNIQUE_NAME_CONSTRAINT, columnNames = ["attribute_name"])])
@Entity
class RoomAttribute(
    @Column(name = "attribute_name", nullable = false)
    var name: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_attribute_id")
    var id: Long? = null
){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomAttribute

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "RoomAttribute(name='$name', id=$id)"
    }

}