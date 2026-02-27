package iee.ihu.gr.pricemonitoringbackend.service

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import iee.ihu.gr.pricemonitoringbackend.CUSTOMERS_EMAIL_UNIQUE_CONSTRAINT
import iee.ihu.gr.pricemonitoringbackend.dto.CustomerRegistrationForm
import iee.ihu.gr.pricemonitoringbackend.dto.PasswordResetForm
import iee.ihu.gr.pricemonitoringbackend.dto.SuccessfulLoginAuthenticationToken
import iee.ihu.gr.pricemonitoringbackend.entity.Email
import iee.ihu.gr.pricemonitoringbackend.entity.Role
import iee.ihu.gr.pricemonitoringbackend.service.user.CustomerService
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.optional.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import java.time.Instant
import java.util.*

class UserServiceITest @Autowired constructor(
    private val customerService: CustomerService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtGenerator: JWTGenerator,
    private val jwtDecoder: JwtDecoder
) : AbstractITest(){

    @Value("\${token.issuer}") private lateinit var expectedIssuer: String
    private val registrationForm = CustomerRegistrationForm("","ANY_NAME","AnyPass123!","AnyPass123!")
    private val passwordResetForm = PasswordResetForm("","NewPass123!","NewPass123!")

    @MethodSource("blankStrings")
    @ParameterizedTest
    fun `should throw ConstraintViolationException when trying to register customer with blank name`(blankName: String) {
        val message = shouldThrow<ConstraintViolationException> { customerService.register(registrationForm.copy(name = blankName)) }.constraintViolations.shouldHaveSize(1).iterator().next().message

        message shouldBe messageSource.getMessage("CustomerForm.name.NotBlank.message")
    }

    @MethodSource("invalidPasswords")
    @ParameterizedTest
    fun `should throw ConstraintViolationException when trying to register customer with invalid password`(invalidPassword: String) {
        val message = shouldThrow<ConstraintViolationException> {
            customerService.register(registrationForm.copy(password = invalidPassword, repeatedPassword = invalidPassword))
        }.constraintViolations.shouldHaveSize(1).iterator().next().message

        message shouldBe messageSource.getMessage("CustomerForm.password.Patter.message")
    }

    @Test
    fun `should throw ConstraintViolationException when trying to register customer and the form contains different passwords`() {
        val message = shouldThrow<ConstraintViolationException> {
            customerService.register(registrationForm.copy( password =  "AnyPass123!", repeatedPassword = "DiffPass123!"))
        }.constraintViolations.shouldHaveSize(1).iterator().next().message

        message shouldBe messageSource.getMessage("CustomerForm.EqualPasswords.message")
    }

    @Test
    fun `should throw InvalidBearerTokenException when trying to use modified token for registration`() {
        val actualEmail = Email("any@email.com")
        val modifiedPayload = """
            {
                "subject":"diffemail@email.com"
            }
        """.trimIndent()
        val token = jwtGenerator.generateEmailJWT(actualEmail)

        shouldThrow<InvalidBearerTokenException> { customerService.register(registrationForm.copy(jwt = token.replacePayload(modifiedPayload))) }
    }

    @Test
    fun `should throw InvalidBearerTokenException when trying to register without jwt`() {
        val notJWT = "This is not a JWT"

        shouldThrow<InvalidBearerTokenException> { customerService.register(registrationForm.copy(jwt = notJWT)) }
    }

    @Test
    fun `should throw InvalidBearerTokenException when trying to register with expired JWT`() {
        val expiredJwt = createExpiredEmailJWS()

        shouldThrow<InvalidBearerTokenException> { customerService.register(registrationForm.copy(jwt = expiredJwt)) }
    }

    @Test
    fun `should register new customer using provided data and email from the token`() {
        val expectedEmail = Email("any@email.com")
        val jwt = jwtGenerator.generateEmailJWT(expectedEmail)
        userRepository.findByEmail(expectedEmail.value).shouldBeEmpty()

        customerService.register(registrationForm.copy(jwt = jwt))

        assertSoftly(userRepository.findByEmail(expectedEmail.value).get()) {
            email shouldBe expectedEmail
            name shouldBe registrationForm.name
            passwordEncoder.matches(registrationForm.password,password).shouldBeTrue()
        }
    }

    @Test
    fun `should throw DataIntegrityViolationException when the same email jwt is used twice`() {
        val email = Email("any@email.com")
        val jwt = jwtGenerator.generateEmailJWT(email)
        customerService.register(registrationForm.copy(jwt = jwt))

        shouldThrow<DataIntegrityViolationException> { customerService.register(registrationForm.copy(jwt = jwt)) }.message shouldContain CUSTOMERS_EMAIL_UNIQUE_CONSTRAINT
    }

    @MethodSource("invalidPasswords")
    @ParameterizedTest
    fun `should throw ConstraintViolationException when trying to submit password reset form with invalid password`(invalidPassword: String) {
        val message = shouldThrow<ConstraintViolationException> {
            customerService.resetPassword(passwordResetForm.copy(password = invalidPassword, repeatedPassword = invalidPassword))
        }.constraintViolations.shouldHaveSize(1).iterator().next().message

        message shouldBe messageSource.getMessage("CustomerForm.password.Patter.message")
    }

    @Test
    fun `should throw ConstraintViolationException when trying to submit password reset form with two different passwords`() {
        val message = shouldThrow<ConstraintViolationException> {
            customerService.resetPassword(passwordResetForm.copy(password = "AnyPass123!", repeatedPassword = "DiffPass123!"))
        }.constraintViolations.shouldHaveSize(1).iterator().next().message

        message shouldBe messageSource.getMessage("CustomerForm.EqualPasswords.message")
    }

    @Test
    fun `should throw InvalidBearerTokenException when trying to use modified token for password reset`() {
        val actualEmail = Email("any@email.com")
        val modifiedPayload = """
            {
                "subject":"diffemail@email.com"
            }
        """.trimIndent()
        val token = jwtGenerator.generatePasswordResetJWT(actualEmail)

        shouldThrow<InvalidBearerTokenException> { customerService.resetPassword(passwordResetForm.copy(jwt = token.replacePayload(modifiedPayload))) }
    }

    @Test
    fun `should throw InvalidBearerTokenException when trying to reset password without jwt`() {
        shouldThrow<InvalidBearerTokenException> { customerService.resetPassword(passwordResetForm.copy(jwt = "Not JWT token.")) }
    }

    @Test
    fun `should throw InvalidBearerTokenException when trying to reset password with expired JWT`() {
        val expiredJWT = createExpiredPasswordResetJWS()

        shouldThrow<InvalidBearerTokenException> { customerService.resetPassword(passwordResetForm.copy(jwt = expiredJWT)) }
    }

    @Test
    fun `should update user password`() {
        registrationForm.password shouldNotBe passwordResetForm.password
        //first create new user
        val email = Email("any@email.com")
        val emailToken = jwtGenerator.generateEmailJWT(email)
        customerService.register(registrationForm.copy(jwt = emailToken))

        val passwordToken = jwtGenerator.generatePasswordResetJWT(email)
        customerService.resetPassword(passwordResetForm.copy(jwt = passwordToken))

        assertSoftly(userRepository.findByEmail(email.value).get()) {
            passwordEncoder.matches(registrationForm.password,password).shouldBeFalse()
            passwordEncoder.matches(passwordResetForm.password,password).shouldBeTrue()
        }
    }

    @Test
    fun `should return false if user does not exist with specified email`() {
        customerService.exists(Email("notUsedEmail@email.com")).shouldBeFalse()
    }

    @Test
    fun `should return true if user with specified email exists`() {
        val email = Email("some@email.com")
        val emailToken = jwtGenerator.generateEmailJWT(email)
        customerService.register(registrationForm.copy(jwt = emailToken))

        customerService.exists(email).shouldBeTrue()
    }

    @Test
    fun `should throw UsernameNotFoundException when trying to authenticated with email that is not used by any user`() {
        val authenticationTokenWithNotUsedEmail = UsernamePasswordAuthenticationToken("notUsed@email.com","ANY")

        shouldThrow<UsernameNotFoundException> { customerService.authenticate(authenticationTokenWithNotUsedEmail) }
    }

    @Test
    fun `should throw BadCredentialsException when trying to authenticated with invalid password`() {
        val email = Email("some@email.com")
        val emailToken = jwtGenerator.generateEmailJWT(email)
        customerService.register(registrationForm.copy(jwt = emailToken))

        shouldThrow<BadCredentialsException> { customerService.authenticate(UsernamePasswordAuthenticationToken(email.value,registrationForm.password.plus("1"))) }
    }

    @EnumSource(Role::class)
    @ParameterizedTest
    fun `should return access token on successful authentication`(role: Role) {
        val email = Email("some@email.com")
        val emailToken = jwtGenerator.generateEmailJWT(email)
        customerService.register(registrationForm.copy(jwt = emailToken))
        userRepository.findByEmail(email.value).get().apply { this.role = role }

        val authentication = customerService.authenticate(UsernamePasswordAuthenticationToken(email.value,registrationForm.password))
        val token = (authentication as SuccessfulLoginAuthenticationToken).bearerToken
        assertSoftly(jwtDecoder.decode(token)) {
            claims["iss"] shouldBe expectedIssuer
            subject shouldBe userRepository.findByEmail(email.value).get().id!!.toString()
            claims["scope"] shouldBe role.name
            claims["name"] shouldBe registrationForm.name
            claims["email"] shouldBe email.value
            expiresAt!!.shouldBeBefore(Instant.now().plusSeconds(60*60*24).plusSeconds(10))
            expiresAt!!.shouldBeAfter(Instant.now().plusSeconds(60*60*24).minusSeconds(10))
        }
    }

    private fun String.replacePayload(newPayload: String) : String{
        val parts = split(".")

        return "${parts[0]}.${String(base64Encoder.encode(newPayload.toByteArray()))}.${parts[2]}"
    }

    private fun createExpiredEmailJWS() : String{
        val macSigner = MACSigner(base64Decoder.decode(key))

        val header = JWSHeader
            .Builder(JWSAlgorithm.HS512)
            .type(JOSEObjectType.JWT)
            .build()

        val claimSet = JWTClaimsSet
            .Builder()
            .issueTime(Date.from(Instant.now()))
            .issuer(issuer)
            .subject("any@email.com")
            .expirationTime(Date.from(Instant.now().minusSeconds(1)))
            .claim("scope", EMAIL_VERIFICATION_CLAIM_VALUE)
            .build()

        return SignedJWT(header,claimSet).apply { sign(macSigner) }.serialize()
    }

    private fun createExpiredPasswordResetJWS() : String{
        val email = Email("any@email.com")
        val macSigner = MACSigner(base64Decoder.decode(key))

        val header = JWSHeader
            .Builder(JWSAlgorithm.HS512)
            .type(JOSEObjectType.JWT)
            .build()

        val claimSet = JWTClaimsSet
            .Builder()
            .issueTime(Date.from(Instant.now()))
            .issuer(issuer)
            .subject(email.value)
            .expirationTime(Date.from(Instant.now().minusSeconds(1)))
            .claim("scope", PASSWORD_RESET_CLAIM_VALUE)
            .build()

        return SignedJWT(header,claimSet).apply { sign(macSigner) }.serialize()
    }
}