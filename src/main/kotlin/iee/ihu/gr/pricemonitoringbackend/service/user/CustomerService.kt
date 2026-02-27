package iee.ihu.gr.pricemonitoringbackend.service.user

import iee.ihu.gr.pricemonitoringbackend.dto.CustomerRegistrationForm
import iee.ihu.gr.pricemonitoringbackend.dto.PasswordResetForm
import iee.ihu.gr.pricemonitoringbackend.entity.User
import iee.ihu.gr.pricemonitoringbackend.entity.Email
import org.springframework.validation.annotation.Validated
import jakarta.validation.Valid
import org.springframework.security.authentication.AuthenticationProvider

@Validated
interface CustomerService : AuthenticationProvider{


    /**
     * Performs customer registration.
     * @throws org.springframework.dao.DataIntegrityViolationException when user with provided email already exists.
     * @throws jakarta.validation.ConstraintViolationException if form is not valid.
     * @throws org.springframework.security.core.AuthenticationException if JWS is invalid or has expired.
     * @return customer object that represents the registered customer.
     */
    fun register(@Valid form: CustomerRegistrationForm) : User

    /**
     * Updates customer's password.
     * @throws jakarta.validation.ConstraintViolationException if form is not valid.
     * @throws org.springframework.security.core.AuthenticationException if JWS is invalid or has expired.
     */
    fun resetPassword(@Valid form: PasswordResetForm)

    /**
     * Returns true if user with this email exists and false otherwise.
     */
    fun exists(email: Email) : Boolean
}