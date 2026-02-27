package iee.ihu.gr.pricemonitoringbackend.entity

import iee.ihu.gr.pricemonitoringbackend.CUSTOMERS_EMAIL_UNIQUE_CONSTRAINT
import jakarta.persistence.*

@Entity
@Table(name = "users",uniqueConstraints = [UniqueConstraint(name = CUSTOMERS_EMAIL_UNIQUE_CONSTRAINT, columnNames = ["email"])])
class User(
    @Column(nullable = false, length = 45)
    var email: Email,

    @Column(nullable = false, length = 45)
    var name: String,

    @Column(nullable = false)
    var password: String,

    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
)

