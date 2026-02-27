package iee.ihu.gr.pricemonitoringbackend.service

import iee.ihu.gr.pricemonitoringbackend.entity.Role
import iee.ihu.gr.pricemonitoringbackend.service.subscription.ConfigurationPropertiesSubscription
import iee.ihu.gr.pricemonitoringbackend.service.subscription.SubscriptionService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.dao.EmptyResultDataAccessException

class SubscriptionServiceITest(private val subscriptionService: SubscriptionService,private val subscriptionProperties: ConfigurationPropertiesSubscription) : AbstractITest() {

    @Test
    fun `should throw EmptyResultDataAccessException when trying to upgrade user that does not exist`() {
        val idOfNotExistingUser = 12301301L
        userRepository.existsById(idOfNotExistingUser).shouldBeFalse()

        shouldThrow<EmptyResultDataAccessException> { subscriptionService.upgradeToBasic(idOfNotExistingUser) }
        shouldThrow<EmptyResultDataAccessException> { subscriptionService.upgradeToPremium(idOfNotExistingUser) }
    }

    @Test
    fun `should upgrade user to basic subscription`() {
        val user = createUser()
        user.role shouldBe Role.USER

        subscriptionService.upgradeToBasic(user.id!!)

        userRepository.findById(user.id!!).get().role shouldBe Role.BASIC
    }

    @Test
    fun `should upgrade user to premium subscription`() {
        Role
            .values()
            .filter { role ->
                when(role){
                    Role.USER,Role.BASIC -> true
                    Role.PREMIUM -> false
                }
            }.forEach { role ->
                val user = createUser(role)

                subscriptionService.upgradeToPremium(user.id!!)

                userRepository.findById(user.id!!).get().role shouldBe Role.PREMIUM
            }
    }

    @Test
    fun `should throw ISE when trying to upgrade user to basic subscription and they already have basic or higher subscription`() {
        Role
            .values()
            .filter { role ->
                when(role){
                    Role.USER -> false
                    Role.BASIC,Role.PREMIUM -> true
                }
            }.forEach { basicOrHigherRole ->
                val user = createUser(basicOrHigherRole)

                shouldThrow<IllegalStateException> { subscriptionService.upgradeToBasic(user.id!!) }

                userRepository.findById(user.id!!).get().role shouldBe basicOrHigherRole
            }
    }

    @Test
    fun `should throw ISE when trying to upgrade user to premium subscription and they already have premium subscription`() {
        Role
            .values()
            .filter { role ->
                when(role){
                    Role.USER,Role.BASIC -> false
                    Role.PREMIUM -> true
                }
            }.forEach { premiumOrHigherRole ->
                val user = createUser(premiumOrHigherRole)

                shouldThrow<IllegalStateException> { subscriptionService.upgradeToPremium(user.id!!) }

                userRepository.findById(user.id!!).get().role shouldBe premiumOrHigherRole
            }
    }

    @Test
    fun `should return the correct subscription limit based on role`() {
        Role
            .values()
            .map { role ->
                when(role){
                    Role.USER -> role to subscriptionProperties.user
                    Role.BASIC -> role to subscriptionProperties.basic
                    Role.PREMIUM -> role to subscriptionProperties.premium
                }
            }.forEach { (role,expectedSubscriptionLimit) ->
                subscriptionService.subscriptionLimits(role) shouldBe expectedSubscriptionLimit
            }
    }
}