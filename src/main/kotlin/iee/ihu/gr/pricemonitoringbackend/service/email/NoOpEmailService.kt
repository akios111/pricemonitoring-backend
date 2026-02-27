package iee.ihu.gr.pricemonitoringbackend.service.email

import iee.ihu.gr.pricemonitoringbackend.dto.Mail
import iee.ihu.gr.pricemonitoringbackend.mask
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("!default")
@Service
class NoOpEmailService : EmailService {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    override fun sendEmail(mail: Mail) {
        logger.info("Imitating email send to {}.Mail Subject:{}, Mail body:{}",mail.recipient.mask(),mail.subject,mail.body)
    }
}