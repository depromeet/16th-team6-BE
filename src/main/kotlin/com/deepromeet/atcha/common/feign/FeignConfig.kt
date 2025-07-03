package com.deepromeet.atcha.common.feign

import com.deepromeet.atcha.transit.infrastructure.client.public.common.config.OpenApiProps
import com.fasterxml.jackson.databind.ObjectMapper
import feign.Retryer
import feign.codec.Decoder
import feign.codec.ErrorDecoder
import org.springframework.beans.factory.ObjectFactory
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.support.SpringDecoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

@Configuration
@EnableConfigurationProperties(OpenApiProps::class)
@EnableFeignClients("com.deepromeet.atcha")
class FeignConfig {
//    @Bean
//    fun feignLoggerLever(): Logger.Level = Logger.Level.FULL

    @Bean
    fun feignDecoder(objectMapper: ObjectMapper): Decoder {
        val convertersFactory: ObjectFactory<HttpMessageConverters> =
            ObjectFactory {
                HttpMessageConverters(
                    listOf<HttpMessageConverter<*>>(
                        MappingJackson2HttpMessageConverter(objectMapper)
                    )
                )
            }

        val springDecoder = SpringDecoder(convertersFactory)
        return LoggerDecoder(springDecoder)
    }

    @Bean
    fun errorDecoder(): ErrorDecoder = CustomErrorDecoder()

    @Bean
    fun retryer() = Retryer.Default(200, 1500, 3)
}
