package com.deepromeet.atcha.transit.infrastructure.cache.config

import com.deepromeet.atcha.shared.infrastructure.cache.config.RedisTemplateFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import java.time.LocalDate

@Configuration
class HolidayRedisConfig {
    @Bean
    fun holidayRedisTemplate(factory: RedisTemplateFactory): RedisTemplate<String, List<LocalDate>> =
        factory.createGeneric()
}
