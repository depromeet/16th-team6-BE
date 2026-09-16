package com.deepromeet.atcha.transit.infrastructure.cache.config

import com.deepromeet.atcha.shared.infrastructure.cache.config.RedisTemplateFactory
import com.deepromeet.atcha.transit.domain.subway.SubwayTimeTable
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class SubwayTimeTableRedisConfig {
    @Bean
    fun subwayTimeTableRedisTemplate(factory: RedisTemplateFactory): RedisTemplate<String, SubwayTimeTable> =
        factory.create(SubwayTimeTable::class.java)
}
