package com.deepromeet.atcha

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class AtchaApplication

fun main(args: Array<String>) {
    runApplication<AtchaApplication>(*args)
}
