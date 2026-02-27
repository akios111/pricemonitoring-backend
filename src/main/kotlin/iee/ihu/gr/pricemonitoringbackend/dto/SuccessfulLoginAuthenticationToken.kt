package iee.ihu.gr.pricemonitoringbackend.dto

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class SuccessfulLoginAuthenticationToken(private val email: String,private val password: String,val bearerToken: String) : AbstractAuthenticationToken(emptyList()) {

    override fun getCredentials(): Any = email

    override fun getPrincipal(): Any = password

}