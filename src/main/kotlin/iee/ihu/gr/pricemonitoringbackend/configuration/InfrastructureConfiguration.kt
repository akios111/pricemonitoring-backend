package iee.ihu.gr.pricemonitoringbackend.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sendgrid.SendGrid
import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.serialization.json.Json
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.security.SecureRandom
import java.util.Base64

@Configuration
class InfrastructureConfiguration {

    @Bean
    fun base64Decoder() : Base64.Decoder = Base64.getDecoder()

    @Bean
    fun java8JacksonModule() : Module = Jdk8Module()

    @Bean
    fun jacksonMessageConverter(objectMapper: ObjectMapper) : Jackson2JsonMessageConverter = Jackson2JsonMessageConverter(objectMapper)

    @Bean
    fun kotlinJacksonModule() : Module = KotlinModule.Builder().build()

    @Bean
    fun jacksonCustomizer() : Jackson2ObjectMapperBuilderCustomizer = Jackson2ObjectMapperBuilderCustomizer { builder ->
        builder.postConfigurer { mapper -> mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false) }
    }

    @Bean
    fun kotlinxJson() : Json = Json { ignoreUnknownKeys = true }

    @Profile("default")
    @Bean
    fun sendGrid(@Value("\${send.grid.api.key}") apiKey: String) : SendGrid = SendGrid(apiKey)

}