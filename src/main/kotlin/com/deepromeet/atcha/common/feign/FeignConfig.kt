package com.deepromeet.atcha.common.feign

import feign.Feign
import feign.Logger
import feign.Request
import feign.Retryer
import feign.codec.ErrorDecoder
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableFeignClients("com.deepromeet.atcha")
class FeignConfig {
    @Bean
    fun feignLoggerLever(): Logger.Level = Logger.Level.FULL

    @Bean
    fun feignBuilder(): Feign.Builder =
        Feign.builder()
            .options(
                Request.Options(
                    Duration.ofMillis(1000),
                    Duration.ofMillis(3000),
                    true
                )
            )

    @Bean
    fun errorDecoder(): ErrorDecoder = CustomErrorDecoder()

    @Bean
    fun retryer() = Retryer.Default(200, 1500, 3)
}
