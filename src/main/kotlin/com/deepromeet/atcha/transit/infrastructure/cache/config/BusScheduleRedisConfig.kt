package com.deepromeet.atcha.transit.infrastructure.cache.config

import com.deepromeet.atcha.shared.infrastructure.cache.config.RedisTemplateFactory
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class BusScheduleRedisConfig {
    @Bean
    fun busTimeTableRedisTemplate(factory: RedisTemplateFactory): RedisTemplate<String, BusSchedule> =
        factory.create(BusSchedule::class.java)
}
