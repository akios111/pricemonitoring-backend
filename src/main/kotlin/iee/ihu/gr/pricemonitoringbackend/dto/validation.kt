package iee.ihu.gr.pricemonitoringbackend.dto

import org.springframework.stereotype.Service
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Constraint(validatedBy = [EqualPasswordsRegistrationFormConstraintValidator::class,EqualPasswordsResetFormValidator::class])
annotation class EqualPasswords(val message: String,val groups: Array<KClass<*>> = [],val payload: Array<KClass<out Payload>> = [])

@Constraint(validatedBy = [NullOrNotBlankConstraintValidator::class])
annotation class NullOrNotBlank(val message: String,val groups: Array<KClass<*>> = [],val payload: Array<KClass<out Payload>> = [])

@Service
class EqualPasswordsRegistrationFormConstraintValidator : ConstraintValidator<EqualPasswords,CustomerRegistrationForm>{
    override fun isValid(value: CustomerRegistrationForm?, context: ConstraintValidatorContext?): Boolean {
        if(value == null) return true

        return value.password == value.repeatedPassword
    }

}

@Service
class EqualPasswordsResetFormValidator : ConstraintValidator<EqualPasswords,PasswordResetForm>{
    override fun isValid(value: PasswordResetForm?, context: ConstraintValidatorContext?): Boolean {
        if(value == null) return true

        return value.password == value.repeatedPassword
    }

}

@Service
class NullOrNotBlankConstraintValidator : ConstraintValidator<NullOrNotBlank,String>{
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if(value == null) return true

        return value.isNotBlank()
    }

}