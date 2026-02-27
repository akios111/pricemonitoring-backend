package iee.ihu.gr.pricemonitoringbackend.service.subscription

import iee.ihu.gr.pricemonitoringbackend.entity.Role
import iee.ihu.gr.pricemonitoringbackend.entity.User

interface SubscriptionService {

    fun upgradeToPremium(userID: Long) : User

    fun upgradeToBasic(userID: Long) : User

    fun subscriptionLimits(role: Role) : SubscriptionLimit
}