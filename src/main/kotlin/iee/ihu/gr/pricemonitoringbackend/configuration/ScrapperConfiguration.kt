package iee.ihu.gr.pricemonitoringbackend.configuration

import org.slf4j.LoggerFactory
import org.springframework.amqp.core.BindingBuilder.*
import org.springframework.amqp.core.ExchangeBuilder.*
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder.*
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer
import org.springframework.amqp.rabbit.connection.ConnectionBlockedEvent
import org.springframework.amqp.rabbit.connection.ConnectionUnblockedEvent
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Scope
import org.springframework.context.event.EventListener
import org.springframework.validation.Validator
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ScrapperConfiguration(private val validator: Validator,private val webClientBuilder: WebClient.Builder) : RabbitListenerConfigurer {


    private val logger = LoggerFactory.getLogger(this::class.java)

    @Value("\${scrapping.response.queue.name}") private lateinit var responsesQueue: String

    @Bean
    fun webClient(@Value("\${scrapping.server.address}") address: String,@Value("\${scrapping.server.port}") port: Int) : WebClient =
        webClientBuilder
            .baseUrl("$address:$port")
            .build()

    @Bean
    fun responseQueue() :Queue = durable(responsesQueue).build()

    @EventListener(ConnectionBlockedEvent::class)
    fun onConnectionBlockedEventListener(event: ConnectionBlockedEvent){
        logger.warn("Connection {} has been blocked by RabbitMQ node. Reason : {}.",event.connection.delegate?.clientProvidedName,event.reason)
    }

    @EventListener(ConnectionUnblockedEvent::class)
    fun onConnectionUnblockedEventListener(event: ConnectionUnblockedEvent){
        logger.info("Connection {} has been unblocked.",event.connection.delegate?.clientProvidedName)
    }

    override fun configureRabbitListeners(registrar: RabbitListenerEndpointRegistrar) {
        registrar.setValidator(validator)
    }


}