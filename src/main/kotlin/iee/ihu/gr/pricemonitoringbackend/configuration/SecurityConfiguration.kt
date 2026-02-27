package iee.ihu.gr.pricemonitoringbackend.configuration

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimNames
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import iee.ihu.gr.pricemonitoringbackend.controller.AUTHORIZATION_COOKIE_NAME
import iee.ihu.gr.pricemonitoringbackend.dto.SuccessfulLoginAuthenticationToken
import iee.ihu.gr.pricemonitoringbackend.entity.Role
import iee.ihu.gr.pricemonitoringbackend.service.user.CustomerService
import jakarta.servlet.http.Cookie
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.config.Customizer
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.context.NullSecurityContextRepository
import org.springframework.security.web.savedrequest.CookieRequestCache
import org.springframework.security.web.savedrequest.RequestCache
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import java.time.Duration
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

@Configuration
class SecurityConfiguration(@Value("\${secret.key}") private val key: String,private val base64: Base64.Decoder) {

    @Bean
    fun passwordEncoder() : PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity,decoder: JwtDecoder,customerService: CustomerService) : SecurityFilterChain =
        http
            .anonymous {}
            .csrf { csrf -> csrf.disable() }
            .cors(withDefaults())
            .logout { logoutConfig -> logoutConfig.disable() }
            .authorizeHttpRequests { authorization ->
                authorization
                    .requestMatchers(HttpMethod.POST,"/customer/login","/customer/register","/customer/email/verification","/customer/password/reset").permitAll()
                    .requestMatchers(HttpMethod.PUT,"/customer/password").permitAll()
                    .requestMatchers(HttpMethod.POST,"/scrapper/login").permitAll()
                    .requestMatchers(HttpMethod.GET,"/","/register","/login","/logout","/test").permitAll()
                    .requestMatchers("/customer/**","/hotel/**","/monitor_list/**","/prices/**","/room/history/**","/subscription/**").hasAnyRole(Role.USER.name,Role.BASIC.name,Role.PREMIUM.name)
                    .anyRequest().permitAll()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.securityContext { context -> context.securityContextRepository(NullSecurityContextRepository()) }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt { jwt ->
                    jwt.decoder(decoder)
                    jwt.jwtAuthenticationConverter(JwtAuthenticationConverter().apply { setJwtGrantedAuthoritiesConverter(JwtGrantedAuthoritiesConverter().apply { setAuthorityPrefix("ROLE_") }) })
                }
            }.build()

    @Bean
    fun decoder() : JwtDecoder = NimbusJwtDecoder
        .withSecretKey(SecretKeySpec(base64.decode(key),JWSAlgorithm.HS512.name))
        .macAlgorithm(MacAlgorithm.HS512)
        .build()

}