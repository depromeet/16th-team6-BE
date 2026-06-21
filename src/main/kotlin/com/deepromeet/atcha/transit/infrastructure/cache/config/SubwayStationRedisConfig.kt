package com.deepromeet.atcha.transit.infrastructure.cache.config

import com.deepromeet.atcha.shared.infrastructure.cache.config.RedisTemplateFactory
import com.deepromeet.atcha.transit.domain.subway.SubwayStation
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class SubwayStationRedisConfig {
    @Bean
    fun subwayStationRedisTemplate(factory: RedisTemplateFactory): RedisTemplate<String, SubwayStation> =
        factory.create(SubwayStation::class.java)
}
