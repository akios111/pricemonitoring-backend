package iee.ihu.gr.pricemonitoringbackend.entity

import iee.ihu.gr.pricemonitoringbackend.ROOM_CHOICE_ATTRIBUTES_UNIQUE_NAME_CONSTRAINT
import jakarta.persistence.*

@Table(name = "room_choice_attributes", uniqueConstraints = [UniqueConstraint(name = ROOM_CHOICE_ATTRIBUTES_UNIQUE_NAME_CONSTRAINT, columnNames = ["attribute_name"])])
@Entity
class RoomViewAttribute (

    @Column(name = "attribute_name", nullable = false)
    var name: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_choice_attribute_id", nullable = false)
    var id: Long? = null
) : Comparable<RoomViewAttribute> {
    override fun compareTo(other: RoomViewAttribute): Int = name.compareTo(other.name)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomViewAttribute

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}