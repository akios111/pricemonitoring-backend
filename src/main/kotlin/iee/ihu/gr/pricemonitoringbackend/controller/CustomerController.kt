package iee.ihu.gr.pricemonitoringbackend.controller

import iee.ihu.gr.pricemonitoringbackend.dto.*
import iee.ihu.gr.pricemonitoringbackend.ipAddress
import iee.ihu.gr.pricemonitoringbackend.service.email.EMAIL_VERIFICATION_SUBJECT
import iee.ihu.gr.pricemonitoringbackend.service.email.EmailService
import iee.ihu.gr.pricemonitoringbackend.service.JWTGenerator
import iee.ihu.gr.pricemonitoringbackend.service.email.PASSWORD_RESET_SUBJECT
import iee.ihu.gr.pricemonitoringbackend.service.rate.RateLimitService
import iee.ihu.gr.pricemonitoringbackend.service.user.CustomerService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestHeader

const val AUTHORIZATION_COOKIE_NAME = "Authorization"

@RequestMapping("/customer")
@RestController
class CustomerController(
    private val customerService: CustomerService,
    private val emailService: EmailService,
    private val jwtGenerator: JWTGenerator,
    private val rateLimitService: RateLimitService
) {

    @PostMapping("/register", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun register(@RequestBody registrationForm: CustomerRegistrationForm) : ResponseEntity<Unit>{
        customerService.register(registrationForm)

        return ResponseEntity.ok(Unit)
    }

    @PostMapping("/login", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun login(@RequestBody customerCredentials: CustomerCredentials) : ResponseEntity<String>{
        val token = customerService.authenticate(UsernamePasswordAuthenticationToken(customerCredentials.email,customerCredentials.password)) as SuccessfulLoginAuthenticationToken

        return ResponseEntity.ok(token.bearerToken)
    }

    @PostMapping("/email/verification", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun emailVerification(@Validated @RequestBody emailData: EmailData,request: HttpServletRequest) : ResponseEntity<Unit> {
        if(!rateLimitService.tryAcquireEmailVerificationPermit(request.ipAddress())){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Unit)
        }
        val jwt = jwtGenerator.generateEmailJWT(emailData.email)

        emailService.sendEmail(Mail(emailData.email, EMAIL_VERIFICATION_SUBJECT,"Your verification token:$jwt"))

        return ResponseEntity.ok(Unit)
    }

    @PostMapping("/password/reset", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun passwordReset(@Validated @RequestBody emailData: EmailData,request: HttpServletRequest) : ResponseEntity<Unit>{
        if(!customerService.exists(emailData.email)) throw EmptyResultDataAccessException(1)

        if(!rateLimitService.tryAcquirePasswordResetPermit(request.ipAddress())){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Unit)
        }

        val jwt = jwtGenerator.generatePasswordResetJWT(emailData.email)

        emailService.sendEmail(Mail(emailData.email, PASSWORD_RESET_SUBJECT,"Your verification token:$jwt"))

        return ResponseEntity.ok(Unit)
    }

    @PutMapping("/password", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updatePassword(@RequestBody passwordResetForm: PasswordResetForm) : ResponseEntity<Unit>{
        customerService.resetPassword(passwordResetForm)

        return ResponseEntity.ok(Unit)
    }

}