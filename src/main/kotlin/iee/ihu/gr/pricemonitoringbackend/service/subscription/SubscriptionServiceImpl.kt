package iee.ihu.gr.pricemonitoringbackend.service.subscription

import iee.ihu.gr.pricemonitoringbackend.entity.Role
import iee.ihu.gr.pricemonitoringbackend.entity.Role.*
import iee.ihu.gr.pricemonitoringbackend.entity.User
import iee.ihu.gr.pricemonitoringbackend.repository.UserRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class SubscriptionServiceImpl(
    private val subscriptionProperties: ConfigurationPropertiesSubscription,

    private val userRepository: UserRepository
) : SubscriptionService {
    override fun upgradeToPremium(userID: Long) : User {
        val user = userRepository.findByIdAndLock(userID).orElseThrow { EmptyResultDataAccessException(1) }

        return when(user.role){
            USER,BASIC -> { user.apply { role = PREMIUM } }
            PREMIUM -> throw IllegalStateException("User already has premium subscription")
        }
    }

    override fun upgradeToBasic(userID: Long) : User {
        val user = userRepository.findByIdAndLock(userID).orElseThrow { EmptyResultDataAccessException(1) }

        return when(user.role){
            USER -> { user.apply { role = BASIC } }
            BASIC,PREMIUM -> throw IllegalStateException("User already has ${user.role.name.lowercase()} subscription.")
        }
    }

    override fun subscriptionLimits(role: Role): SubscriptionLimit = when(role){
        USER -> subscriptionProperties.user
        BASIC -> subscriptionProperties.basic
        PREMIUM -> subscriptionProperties.premium
    }
}