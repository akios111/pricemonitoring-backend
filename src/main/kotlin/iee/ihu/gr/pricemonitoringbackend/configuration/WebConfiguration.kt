package iee.ihu.gr.pricemonitoringbackend.configuration

import iee.ihu.gr.pricemonitoringbackend.mvc.ThrottlingFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfiguration : WebMvcConfigurer {

    @Bean
    fun throttlingFilterRegistrationBean(throttlingFilter: ThrottlingFilter) : FilterRegistrationBean<ThrottlingFilter>{
        return FilterRegistrationBean(throttlingFilter).apply {
            addUrlPatterns("/*")
        }
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedMethods("*")
            .allowedOrigins("*")
            .allowedHeaders("*")
    }
}