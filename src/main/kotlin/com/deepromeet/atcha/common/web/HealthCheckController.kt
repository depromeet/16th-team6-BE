package com.deepromeet.atcha.common.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class HealthCheckController {
    @GetMapping("/health")
    fun healthCheck(): String {
        return "I'm OK"
    }
}
