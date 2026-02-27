package iee.ihu.gr.pricemonitoringbackend.service.email

import iee.ihu.gr.pricemonitoringbackend.dto.Mail
import iee.ihu.gr.pricemonitoringbackend.entity.Email

interface EmailService {

    fun sendEmail(mail: Mail)

}