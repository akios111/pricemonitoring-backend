package iee.ihu.gr.pricemonitoringbackend.controller

import iee.ihu.gr.pricemonitoringbackend.CUSTOMERS_EMAIL_UNIQUE_CONSTRAINT
import iee.ihu.gr.pricemonitoringbackend.dto.*
import iee.ihu.gr.pricemonitoringbackend.entity.Email
import iee.ihu.gr.pricemonitoringbackend.service.email.EMAIL_VERIFICATION_SUBJECT
import iee.ihu.gr.pricemonitoringbackend.service.email.PASSWORD_RESET_SUBJECT
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.*
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class UserControllerUnitTest : AbstractControllerUnitTest() {

    private val registrationForm = CustomerRegistrationForm("JWT","ANY_NAME","ANY_PASS","ANY_PASS")

    private val credentials = CustomerCredentials("ANY_EMAIL","ANY_PASSWORD")

    private val emailData = EmailData(Email("test@email.com"))

    private val passwordResetForm = PasswordResetForm("ANY_JWT","ANY_PASS","ANY_PASS")

    @Test
    fun `should return 200(OK) response after successful customer registration`() {
        every { customerServiceMock.register(registrationForm) } returns mockk()

        mockMvc.post("/customer/register"){
            contentType = MediaType.APPLICATION_JSON
            content = json.encodeToString(registrationForm)
        }.andExpect {
            status { isOk() }
        }

        verifyAll { customerServiceMock.register(registrationForm) }
    }

    @MethodSource("allExceptPostMethods")
    @ParameterizedTest
    fun `should return 401(Unauthorized) when trying to register with invalid http method`(method: HttpMethod) {
        mockMvc
            .perform{ request(method,"/customer/register").buildRequest(it) }
            .andExpect(status().isUnauthorized)

        verifyAll { customerServiceMock wasNot called }
    }

    @MethodSource("allExceptPostMethods")
    @ParameterizedTest
    fun `should return 401(Unauthorized) when trying to login with invalid http method`(method: HttpMethod) {
        mockMvc
            .perform{ request(method,"/customer/login").buildRequest(it) }
            .andExpect(status().isUnauthorized)

        verifyAll { customerServiceMock wasNot called }
    }

    @MethodSource("mediaTypesWithoutJson")
    @ParameterizedTest
    fun `should return 415(Unsupported Media Type) when trying to register with invalid content type`(notJsonMediaType: String) {
        mockMvc
            .perform{ request(HttpMethod.POST,"/customer/register").contentType(notJsonMediaType).buildRequest(it) }
            .andExpect(status().isUnsupportedMediaType)
    }

    @MethodSource("mediaTypesWithoutJson")
    @ParameterizedTest
    fun `should return 415(Unsupported Media Type) when trying to login with invalid content type`(notJsonMediaType: String) {
        mockMvc
            .perform{ request(HttpMethod.POST,"/customer/login").contentType(notJsonMediaType).buildRequest(it) }
            .andExpect(status().isUnsupportedMediaType)
    }

    @Test
    fun `should return 409 response when registering user with duplicate email`() {
        every { customerServiceMock.register(registrationForm) } throws DataIntegrityViolationException(CUSTOMERS_EMAIL_UNIQUE_CONSTRAINT)

        mockMvc.post("/customer/register"){
            contentType = MediaType.APPLICATION_JSON
            content = json.encodeToString(registrationForm)
        }.andExpect {
            status { isConflict() }
        }

        verifyAll {
            customerServiceMock.register(registrationForm)
        }
    }

    @Test
    fun `should return 200(OK) response after successful login with token in the body`() {
        val expectedToken = "SOME_TOKEN"
        every {
            customerServiceMock.authenticate(UsernamePasswordAuthenticationToken(credentials.email,credentials.password))
        } returns SuccessfulLoginAuthenticationToken(credentials.email,credentials.password,expectedToken)

        mockMvc.post("/customer/login"){
            contentType = MediaType.APPLICATION_JSON
            content = json.encodeToString(credentials)
        }.andExpect {
            status { isOk() }
            content { string(expectedToken) }
        }

        verifyAll { customerServiceMock.authenticate(UsernamePasswordAuthenticationToken(credentials.email,credentials.password)) }
    }

    @Test
    fun `should return 401 response with empty body when trying to authenticate with invalid credentials`() {
        every {
            customerServiceMock.authenticate(UsernamePasswordAuthenticationToken(credentials.email,credentials.password))
        } throws BadCredentialsException("")
        mockMvc.post("/customer/login"){
            contentType = MediaType.APPLICATION_JSON
            content = json.encodeToString(credentials)
        }.andExpect {
            status { isUnauthorized() }
            content { string("") }
        }

        verifyAll { customerServiceMock.authenticate(UsernamePasswordAuthenticationToken(credentials.email,credentials.password)) }
    }

    @MethodSource("invalidEmails")
    @ParameterizedTest
    fun `should return 400(Bad Request) when trying to send email verification for invalid email`(invalidEmail: String) {
        mockMvc
            .post("/customer/email/verification"){
                content = json.encodeToString(EmailData(Email(invalidEmail)))
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
            }

        verifyAll {
            customerServiceMock wasNot called
            emailServiceMock wasNot called
            jwtGeneratorMock wasNot called
        }
    }

    @MethodSource("mediaTypesWithoutJson")
    @ParameterizedTest
    fun `should return 415(Unsupported media type) when trying to send verification email with unsupported request content type`(invalidContentType: String) {
        mockMvc
            .post("/customer/email/verification"){
                content = json.encodeToString(emailData)
                contentType = MediaType.parseMediaType(invalidContentType)
            }.andExpect {
                status { isUnsupportedMediaType() }
            }

        verifyAll {
            customerServiceMock wasNot called
            emailServiceMock wasNot called
            jwtGeneratorMock wasNot called
        }
    }

    @MethodSource("allExceptPostMethods")
    @ParameterizedTest
    fun `should return 401(Unauthorized) when trying to request email verification with invalid http method`(invalidMethod: HttpMethod) {
        mockMvc
            .perform { request(invalidMethod,"/customer/email/verification").buildRequest(it) }
            .andExpect(status().isUnauthorized)

        verifyAll {
            customerServiceMock wasNot called
            emailServiceMock wasNot called
            jwtGeneratorMock wasNot called
        }
    }

    @Test
    fun `should return 200(OK) response and send email verification`() {
        every { rateLimitServiceMock.tryAcquireEmailVerificationPermit("127.0.0.1") } returns true
        val jwt = "ANY_JWT"
        val mailSent = slot<Mail>()
        every { jwtGeneratorMock.generateEmailJWT(emailData.email) } returns jwt
        every { emailServiceMock.sendEmail(capture(mailSent)) } just runs

        mockMvc
            .post("/customer/email/verification"){
                content = json.encodeToString(emailData)
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
            }

        verify{
            rateLimitServiceMock.tryAcquireEmailVerificationPermit("127.0.0.1")
        }

        verifyAll {
            customerServiceMock wasNot called
            jwtGeneratorMock.generateEmailJWT(emailData.email)
            emailServiceMock.sendEmail(any())
        }
        assertSoftly(mailSent.captured) {
            recipient shouldBe emailData.email
            subject shouldBe EMAIL_VERIFICATION_SUBJECT
            body shouldContain jwt
        }
    }

    @MethodSource("invalidEmails")
    @ParameterizedTest
    fun `should return 400(Bad Request) when trying to request password reset for invalid email`(invalidEmail: String) {
        mockMvc
            .post("/customer/password/reset"){
                content = json.encodeToString(EmailData(Email(invalidEmail)))
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
            }

        verifyAll {
            customerServiceMock wasNot called
            emailServiceMock wasNot called
            jwtGeneratorMock wasNot called
        }
    }

    @MethodSource("mediaTypesWithoutJson")
    @ParameterizedTest
    fun `should return 415(Unsupported Media Type) when trying to request password reset with invalid request content type`(invalidContentType: String) {
        mockMvc
            .post("/customer/password/reset"){
                content = json.encodeToString(emailData)
                contentType = MediaType.parseMediaType(invalidContentType)
            }.andExpect {
                status { isUnsupportedMediaType() }
            }

        verifyAll {
            customerServiceMock wasNot called
            emailServiceMock wasNot called
            jwtGeneratorMock wasNot called
        }
    }

    @MethodSource("allExceptPostMethods")
    @ParameterizedTest
    fun `should return 401(Unauthorized) when trying to request password reset with invalid http method`(invalidHttpMethod: HttpMethod) {
        mockMvc
            .perform { request(invalidHttpMethod,"/customer/password/reset").buildRequest(it) }
            .andExpect(status().isUnauthorized)

        verifyAll {
            customerServiceMock wasNot called
            emailServiceMock wasNot called
            jwtGeneratorMock wasNot called
        }
    }

    @Test
    fun `should return 404(Not Found) when trying to request password reset with email that is not used by any user`() {
        every { customerServiceMock.exists(emailData.email) } returns false

        mockMvc
            .post("/customer/password/reset"){
                content = json.encodeToString(emailData)
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isNotFound() }
            }

        verifyAll {
            customerServiceMock.exists(emailData.email)
            emailServiceMock wasNot called
            jwtGeneratorMock wasNot called
        }
    }

    @Test
    fun `should return 200(OK) response after sending password reset token to email`() {
        every { rateLimitServiceMock.tryAcquirePasswordResetPermit("127.0.0.1") } returns true
        val jwt = "ANY_JWT"
        val mailSent = slot<Mail>()
        every { customerServiceMock.exists(emailData.email) } returns true
        every { jwtGeneratorMock.generatePasswordResetJWT(emailData.email) } returns jwt
        every { emailServiceMock.sendEmail(capture(mailSent)) } just runs

        mockMvc
            .post("/customer/password/reset"){
                content = json.encodeToString(emailData)
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
            }

        verify{
            rateLimitServiceMock.tryAcquirePasswordResetPermit("127.0.0.1")
        }

        verifyAll {
            customerServiceMock.exists(emailData.email)
            jwtGeneratorMock.generatePasswordResetJWT(emailData.email)
            emailServiceMock.sendEmail(any())
        }
        assertSoftly(mailSent.captured) {
            recipient shouldBe emailData.email
            subject shouldBe PASSWORD_RESET_SUBJECT
            body shouldContain jwt
        }
    }

    @MethodSource("allExceptPutMethods")
    @ParameterizedTest
    fun `should return 401(Unauthorized) when trying to send update password request with invalid http method`(invalidHttpMethod: HttpMethod) {
        mockMvc
            .perform { request(invalidHttpMethod,"/customer/password").buildRequest(it) }
            .andExpect(status().isUnauthorized)

        verifyAll {
            customerServiceMock wasNot called
            emailServiceMock wasNot called
            jwtGeneratorMock wasNot called
        }
    }

    @MethodSource("mediaTypesWithoutJson")
    @ParameterizedTest
    fun `should return 415(Unsupported Media Type) when trying to send update password request with invalid content type`(invalidContentType: String) {
        mockMvc
            .put("/customer/password"){
                content = json.encodeToString(passwordResetForm)
                contentType = MediaType.parseMediaType(invalidContentType)
            }.andExpect {
                status { isUnsupportedMediaType() }
            }

        verifyAll {
            customerServiceMock wasNot called
            emailServiceMock wasNot called
            jwtGeneratorMock wasNot called
        }
    }

    @Test
    fun `should return 200(OK) response after updating customer password`() {
        every { customerServiceMock.resetPassword(passwordResetForm) } just runs

        mockMvc
            .put("/customer/password"){
                content = json.encodeToString(passwordResetForm)
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
            }

        verifyAll {
            customerServiceMock.resetPassword(passwordResetForm)
            emailServiceMock wasNot called
            jwtGeneratorMock wasNot called
        }
    }

    @Test
    fun `should return 429(Too many requests) when trying to send email verification and rate limit has been reached`() {
        every { rateLimitServiceMock.tryAcquireEmailVerificationPermit("127.0.0.1") } returns false

        mockMvc
            .post("/customer/email/verification"){
                content = json.encodeToString(emailData)
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isTooManyRequests() }
            }

        verify{
            rateLimitServiceMock.tryAcquireEmailVerificationPermit("127.0.0.1")
        }

        verifyAll {
            customerServiceMock wasNot called
            emailServiceMock wasNot called
            jwtGeneratorMock wasNot called
        }
    }

    @Test
    fun `should return 429(Too many requests) when trying to send password reset email and rate limit has been reached`() {
        every { rateLimitServiceMock.tryAcquirePasswordResetPermit("127.0.0.1") } returns false
        every { customerServiceMock.exists(emailData.email) } returns true

        mockMvc
            .post("/customer/password/reset"){
                content = json.encodeToString(emailData)
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isTooManyRequests() }
            }
        verify{
            rateLimitServiceMock.tryAcquirePasswordResetPermit("127.0.0.1")
        }

        verifyAll {
            customerServiceMock.exists(emailData.email)
            emailServiceMock wasNot called
            jwtGeneratorMock wasNot called
        }
    }
}