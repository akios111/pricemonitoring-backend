package iee.ihu.gr.pricemonitoringbackend.service

import com.nimbusds.jwt.JWTClaimsSet
import iee.ihu.gr.pricemonitoringbackend.entity.User
import iee.ihu.gr.pricemonitoringbackend.entity.Email

interface JWTGenerator {

    fun generate(user: User) : String

    fun generateEmailJWT(email: Email) : String

    fun generatePasswordResetJWT(email: Email) : String

    fun verify(token: String, expectedScope: String, expectedSubject: String? = null) : JWTClaimsSet
}