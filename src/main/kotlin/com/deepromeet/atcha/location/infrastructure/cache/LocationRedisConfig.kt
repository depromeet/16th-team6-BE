package com.deepromeet.atcha.location.infrastructure.cache

import com.deepromeet.atcha.location.domain.POI
import com.deepromeet.atcha.shared.infrastructure.cache.config.RedisTemplateFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class LocationRedisConfig {
    @Bean
    fun poiHistoryRedisTemplate(factory: RedisTemplateFactory): RedisTemplate<String, POI> =
        factory.create(POI::class.java)
}
