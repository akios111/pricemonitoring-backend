package iee.ihu.gr.pricemonitoringbackend.service.user

import iee.ihu.gr.pricemonitoringbackend.dto.CustomerRegistrationForm
import iee.ihu.gr.pricemonitoringbackend.dto.PasswordResetForm
import iee.ihu.gr.pricemonitoringbackend.dto.SuccessfulLoginAuthenticationToken
import iee.ihu.gr.pricemonitoringbackend.entity.User
import iee.ihu.gr.pricemonitoringbackend.entity.Email
import iee.ihu.gr.pricemonitoringbackend.repository.UserRepository
import iee.ihu.gr.pricemonitoringbackend.service.EMAIL_VERIFICATION_CLAIM_VALUE
import iee.ihu.gr.pricemonitoringbackend.service.JWTGenerator
import iee.ihu.gr.pricemonitoringbackend.service.PASSWORD_RESET_CLAIM_VALUE
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CustomerServiceImpl(
    private val jwtGenerator: JWTGenerator,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository
) : CustomerService {

    override fun register(form: CustomerRegistrationForm): User {
        val jwtClaimsSet = jwtGenerator.verify(form.jwt,EMAIL_VERIFICATION_CLAIM_VALUE)
        val email = Email(jwtClaimsSet.subject)

        return userRepository.save(with(form) { User(email, name, passwordEncoder.encode(password)) })
    }

    override fun resetPassword(form: PasswordResetForm) {
        val jwtClaimSet = jwtGenerator.verify(form.jwt, PASSWORD_RESET_CLAIM_VALUE)

        val customer = userRepository.findByEmail(jwtClaimSet.subject).orElseThrow { EmptyResultDataAccessException(1) }

        customer.password = passwordEncoder.encode(form.password)
    }

    @Transactional(readOnly = true)
    override fun exists(email: Email): Boolean = userRepository.findByEmail(email.value).map { true }.orElse(false)

    @Transactional(readOnly = true)
    override fun authenticate(authentication: Authentication): Authentication {
        val email = authentication.principal as String
        val password = authentication.credentials as String
        val customer = userRepository
            .findByEmail(email)
            .orElseThrow { UsernameNotFoundException("User with email $email not found.") }

        if (!passwordEncoder.matches(password, customer.password)) {
            throw BadCredentialsException("Invalid credentials!")
        }

        val token = jwtGenerator.generate(customer)

        return SuccessfulLoginAuthenticationToken(email,"",token)
    }
    override fun supports(authentication: Class<*>): Boolean = UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)

}