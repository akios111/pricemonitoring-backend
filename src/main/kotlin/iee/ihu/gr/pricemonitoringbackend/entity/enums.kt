package iee.ihu.gr.pricemonitoringbackend.entity

enum class Role{ USER,BASIC,PREMIUM }

enum class HotelType{ PRIVATE_HOST,HOTEL,APART_HOTEL,HOLIDAY_HOME,GUEST_HOUSE }

enum class CancellationPolicy{
    FREE_CANCELLATION,REFUNDABLE,NON_REFUNDABLE,PARTIALLY_REFUNDABLE,UNKNOWN;
}

enum class MonitorListStatus{ ACTIVE,INACTIVE }

enum class RateLimitRefillStrategy{ INTERVAL,GREEDY }