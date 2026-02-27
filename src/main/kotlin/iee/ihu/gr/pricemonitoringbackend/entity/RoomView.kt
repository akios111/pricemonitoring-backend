package iee.ihu.gr.pricemonitoringbackend.entity

import iee.ihu.gr.pricemonitoringbackend.HOTEL_ROOM_CHOICE_UNIQUE_CONSTRAINT
import java.time.LocalDateTime
import jakarta.persistence.*
import org.hibernate.envers.Audited
import org.hibernate.envers.RelationTargetAuditMode
import java.util.SortedSet
import java.util.TreeSet

@Table(name = "hotel_room_choices", uniqueConstraints = [UniqueConstraint(name = HOTEL_ROOM_CHOICE_UNIQUE_CONSTRAINT, columnNames = ["hotel_room_id","cancellation_policy","breakfast","sleeps"])])
@Entity
class RoomView (
    @Column(name = "started_on", nullable = false, columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP")
    var startedOn: LocalDateTime,

    @Column(name = "sleeps", nullable = false, columnDefinition = "tinyint")
    var sleeps: Int,

    @JoinColumn(name = "hotel_room_id", foreignKey = ForeignKey(ConstraintMode.CONSTRAINT, name = "fk_hotel_room_choices_hotel_rooms", foreignKeyDefinition = "FOREIGN KEY(hotel_room_id) REFERENCES hotel_rooms(id) ON DELETE NO ACTION ON UPDATE NO ACTION"))
    @ManyToOne(optional = false)
    var room: Room,

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_policy", nullable = false)
    var cancellationPolicy: CancellationPolicy,

    @Column(name = "breakfast", nullable = false)
    var breakfast: String,

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JoinTable(name = "hotel_room_choices_owned_attributes", joinColumns = [JoinColumn(name = "hotel_room_choice_id")], inverseJoinColumns = [JoinColumn(name = "room_choice_attribute_id")])
    @ManyToMany
    var attributes: SortedSet<RoomViewAttribute> = TreeSet(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomView

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}