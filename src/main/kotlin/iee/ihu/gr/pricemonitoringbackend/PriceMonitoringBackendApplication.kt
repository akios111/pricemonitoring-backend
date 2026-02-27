package iee.ihu.gr.pricemonitoringbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan(basePackageClasses = [PriceMonitoringBackendApplication::class])
@SpringBootApplication
class PriceMonitoringBackendApplication



fun main(args: Array<String>) {
    runApplication<PriceMonitoringBackendApplication>(*args){ addListeners(ApplicationPidFileWriter()) }
}

