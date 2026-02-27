package iee.ihu.gr.pricemonitoringbackend.service

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.nimbusds.jose.proc.BadJOSEException
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimNames
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import iee.ihu.gr.pricemonitoringbackend.entity.User
import iee.ihu.gr.pricemonitoringbackend.entity.Email
import iee.ihu.gr.pricemonitoringbackend.service.subscription.ConfigurationPropertiesSubscription
import iee.ihu.gr.pricemonitoringbackend.service.subscription.SubscriptionService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.*

const val EMAIL_VERIFICATION_CLAIM_VALUE = "email_verification"
const val PASSWORD_RESET_CLAIM_VALUE = "password_reset"

@Service
class JWTGeneratorImpl(
    private val base64: Base64.Decoder,

    @Value("\${secret.key}") private val key: String,

    private val subscriptionService: SubscriptionService
) : JWTGenerator {

    private val macSigner = MACSigner(base64.decode(key))

    @Value("\${confirmation.token.duration}")
    private lateinit var confirmationTokenDuration: Duration

    @Value("\${token.duration}")
    private lateinit var authenticationTokenDuration: Duration

    @Value("\${token.issuer}")
    private lateinit var issuer: String

    override fun generate(user: User): String {
        val header = createJWSHeader()

        val subscriptionLimits = subscriptionService.subscriptionLimits(user.role)

        val claimSet = JWTClaimsSet
            .Builder()
            .issueTime(Date.from(Instant.now()))
            .issuer(issuer)
            .subject(user.id!!.toString())
            .expirationTime(Date.from(Instant.now().plus(authenticationTokenDuration)))
            .claim("scope",user.role.name)
            .claim("name",user.name)
            .claim("email",user.email.value)
            .claim("maxMonitorList",subscriptionLimits.maxMonitorList)
            .claim("maxRooms",subscriptionLimits.maxRooms.map(Int::toString).orElse("unlimited"))
            .build()

        return SignedJWT(header,claimSet).apply { sign(macSigner) }.serialize()
    }

    override fun generateEmailJWT(email: Email): String {
        val header = createJWSHeader()

        val claimSet = JWTClaimsSet
            .Builder()
            .issueTime(Date.from(Instant.now()))
            .issuer(issuer)
            .subject(email.value)
            .expirationTime(Date.from(Instant.now().plus(confirmationTokenDuration)))
            .claim("scope", EMAIL_VERIFICATION_CLAIM_VALUE)
            .build()

        return SignedJWT(header,claimSet).apply { sign(macSigner) }.serialize()
    }

    @Transactional(readOnly = true)
    override fun generatePasswordResetJWT(email: Email): String {
        val header = createJWSHeader()

        val claimSet = JWTClaimsSet
            .Builder()
            .issueTime(Date.from(Instant.now()))
            .issuer(issuer)
            .subject(email.value)
            .expirationTime(Date.from(Instant.now().plus(confirmationTokenDuration)))
            .claim("scope", PASSWORD_RESET_CLAIM_VALUE)
            .build()

        return SignedJWT(header,claimSet).apply { sign(macSigner) }.serialize()
    }

    override fun verify(token: String, expectedScope: String, expectedSubject: String?): JWTClaimsSet {
        val jwtProcessor = DefaultJWTProcessor<SecurityContext>().apply {
            jweTypeVerifier = DefaultJOSEObjectTypeVerifier(JOSEObjectType.JWT)
            jwsKeySelector = JWSVerificationKeySelector(JWSAlgorithm.HS512, ImmutableSecret(base64.decode(key)))
            jwtClaimsSetVerifier = DefaultJWTClaimsVerifier(
                JWTClaimsSet.Builder().apply {
                    issuer(issuer)
                    claim("scope",expectedScope)
                    if(expectedSubject != null) subject(expectedSubject)
                }.build(),
                setOf(JWTClaimNames.EXPIRATION_TIME,JWTClaimNames.ISSUED_AT)
            )
        }

        val jwt = try{
            JWTParser.parse(token)
        }catch (ex: Exception){
            throw InvalidBearerTokenException("An error occurred while attempting to decode the Jwt:${ex.message}.",ex)
        }

        val claimSet = try{
            jwtProcessor.process(jwt,null)
        }catch (ex: JOSEException){
            throw AuthenticationServiceException("An error occurred while attempting to decode the Jwt:${ex.message}.",ex)
        }catch (ex: BadJOSEException){
            throw InvalidBearerTokenException("An error occurred while attempting to decode the Jwt:${ex.message}.",ex)
        }

        if(Instant.now().isAfter(claimSet.expirationTime.toInstant()))
            throw InvalidBearerTokenException("Token has been expired")

        return claimSet
    }

    private fun createJWSHeader() : JWSHeader = JWSHeader
        .Builder(JWSAlgorithm.HS512)
        .type(JOSEObjectType.JWT)
        .build()

}