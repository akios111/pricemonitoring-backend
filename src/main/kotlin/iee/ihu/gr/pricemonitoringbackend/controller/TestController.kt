package iee.ihu.gr.pricemonitoringbackend.controller

import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Profile("test")
@RequestMapping("/test")
@RestController
class TestController {

    @GetMapping
    fun test() : ResponseEntity<Unit> = ResponseEntity.ok(Unit)

}