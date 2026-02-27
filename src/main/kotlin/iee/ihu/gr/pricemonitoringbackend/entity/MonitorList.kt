package iee.ihu.gr.pricemonitoringbackend.entity

import iee.ihu.gr.pricemonitoringbackend.MONITOR_LISTS_UNIQUE_NAME_PER_OWNER
import java.time.LocalDate
import jakarta.persistence.*

@Table(name = "monitor_list", uniqueConstraints = [UniqueConstraint(name = MONITOR_LISTS_UNIQUE_NAME_PER_OWNER, columnNames = ["user_id","monitor_list_name"])])
@Entity
class MonitorList(
    @JoinTable(
        name = "monitor_list_details",
        joinColumns = [JoinColumn(name = "monitor_list_id")],
        inverseJoinColumns = [JoinColumn(name = "room_id")]
    )
    @ManyToMany
    var rooms: MutableSet<Room> = mutableSetOf(),

    @Column(name = "monitor_list_name", length = 45)
    var name: String,

    @JoinColumn(name = "user_id")
    @ManyToOne(optional = false)
    var owner: User,

    @Column(name = "distance_days")
    @CollectionTable(name = "distances", joinColumns = [JoinColumn(name = "monitor_list_id")])
    @ElementCollection
    var distances: MutableSet<Int> = mutableSetOf(),

    @Enumerated(EnumType.STRING)
    var status: MonitorListStatus = MonitorListStatus.ACTIVE,

    @Column(name = "creation_date")
    var creationDate: LocalDate = LocalDate.now(),

    @Column(name = "monitoring_type", nullable = true, length = 15)
    var monitoringType: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
)

