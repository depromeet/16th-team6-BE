package com.deepromeet.atcha.transit.infrastructure.cache.config

import com.deepromeet.atcha.shared.infrastructure.cache.config.RedisTemplateFactory
import com.deepromeet.atcha.transit.domain.subway.Route
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class SubwayRouteRedisConfig {
    @Bean
    fun subwayRouteRedisTemplate(factory: RedisTemplateFactory): RedisTemplate<String, List<Route>> =
        factory.create(object : TypeReference<List<Route>>() {})
}
