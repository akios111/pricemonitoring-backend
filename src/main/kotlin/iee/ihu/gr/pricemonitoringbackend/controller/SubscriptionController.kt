package iee.ihu.gr.pricemonitoringbackend.controller

import iee.ihu.gr.pricemonitoringbackend.service.JWTGenerator
import iee.ihu.gr.pricemonitoringbackend.service.subscription.SubscriptionService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/subscription")
@RestController
class SubscriptionController(private val subscriptionService: SubscriptionService,private val jwtGenerator: JWTGenerator) {

    @PostMapping("/basic")
    fun basic() : ResponseEntity<String>{
        return try{
            val user = subscriptionService.upgradeToBasic(SecurityContextHolder.getContext().authentication.name.toLong())
            ResponseEntity.ok(jwtGenerator.generate(user))
        }catch (ex: IllegalStateException){
            ResponseEntity.badRequest().body(ex.message)
        }
    }

    @PostMapping("/premium")
    fun premium() : ResponseEntity<String>{
        return try{
            val user = subscriptionService.upgradeToPremium(SecurityContextHolder.getContext().authentication.name.toLong())
            ResponseEntity.ok(jwtGenerator.generate(user))
        }catch (ex: IllegalStateException){
            ResponseEntity.badRequest().body(ex.message)
        }
    }

}