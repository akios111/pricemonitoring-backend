package iee.ihu.gr.pricemonitoringbackend.service.email

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import iee.ihu.gr.pricemonitoringbackend.EmailProviderLimitExceededException
import iee.ihu.gr.pricemonitoringbackend.dto.Mail
import iee.ihu.gr.pricemonitoringbackend.mask
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.IOException

const val EMAIL_VERIFICATION_SUBJECT = "Price Monitoring Verification Token"
const val PASSWORD_RESET_SUBJECT = "Price Monitoring Password Reset"

@Profile("default")
@Service
class SendGridEmailService(private val sendGrid: SendGrid, @Value("\${send.grid.from}") from: String) : EmailService {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    private val fromEmail = Email(from)

    override fun sendEmail(mail: Mail) {
        val sendGridMail = com.sendgrid.helpers.mail.Mail(fromEmail,mail.subject,Email(mail.recipient.value), Content("text/plain",mail.body))
        val request = Request().apply {
            method = Method.POST
            endpoint = "mail/send"
            body = sendGridMail.build()
        }

        try{
            val response = sendGrid.api(request)
            if(response.statusCode == 202){
                logger.info("Successfully sent email to {}",mail.recipient.mask())
            }
            if(response.statusCode != 202){
                logger.error("Received error from send grid api call.Status code:{},Body : {}",response.statusCode,response.body)
            }
            if(response.statusCode == 429){
                logger.warn("Reached send grid limit.")
                throw EmailProviderLimitExceededException()
            }
        }catch (ex: IOException){
            logger.error("Error sending mail with send grid api",ex)
            throw ex
        }

    }

}