package iee.ihu.gr.pricemonitoringbackend.service.subscription

import iee.ihu.gr.pricemonitoringbackend.entity.Role
import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.Optional

@ConfigurationProperties("app.subscription")
class ConfigurationPropertiesSubscription(val user: SubscriptionLimit, val basic: SubscriptionLimit, val premium: SubscriptionLimit)

data class SubscriptionLimit(val maxMonitorList: Int,val maxRooms: Optional<Int> = Optional.empty())
