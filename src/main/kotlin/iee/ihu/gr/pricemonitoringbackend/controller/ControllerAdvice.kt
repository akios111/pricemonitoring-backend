package iee.ihu.gr.pricemonitoringbackend.controller

import iee.ihu.gr.pricemonitoringbackend.CUSTOMERS_EMAIL_UNIQUE_CONSTRAINT
import iee.ihu.gr.pricemonitoringbackend.EmailProviderLimitExceededException
import iee.ihu.gr.pricemonitoringbackend.HOTEL_LISTS_UNIQUE_HOTEL_LIST_NAME_PER_OWNER_CONSTRAINT
import iee.ihu.gr.pricemonitoringbackend.MONITOR_LISTS_UNIQUE_NAME_PER_OWNER
import org.apache.coyote.Response
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.lang.IllegalStateException
import jakarta.validation.ConstraintViolationException

@ControllerAdvice
class ControllerAdvice(private val messageSource: MessageSource) {

    @ExceptionHandler(ConstraintViolationException::class)
    fun constraintViolationException(ex: ConstraintViolationException) : ResponseEntity<List<String>> = ResponseEntity.badRequest().body(ex.constraintViolations.map { it.message })

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun dataIntegrityViolationException(ex: DataIntegrityViolationException) : ResponseEntity<String>{
        return when{
            ex.message?.contains(CUSTOMERS_EMAIL_UNIQUE_CONSTRAINT) == true -> ResponseEntity.status(HttpStatus.CONFLICT).body(messageSource.getMessage("Customer.Unique.Email.Constraint.Violation.message", emptyArray(),LocaleContextHolder.getLocale()))
            else -> throw ex
        }
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArgumentExceptionHandler(ex: IllegalArgumentException) : ResponseEntity<String> = ResponseEntity.badRequest().body(ex.message)

    @ExceptionHandler(IllegalStateException::class)
    fun illegalStateException(ex: Exception) : ResponseEntity<String> = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

    @ExceptionHandler(EmptyResultDataAccessException::class)
    fun emptyResultDataAccessException(ex: EmptyResultDataAccessException) : ResponseEntity<String> = ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message)

    @ExceptionHandler(EmailProviderLimitExceededException::class)
    fun emailProviderLimitExceededExceptionHandler() : ResponseEntity<Unit> = ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Unit)
}