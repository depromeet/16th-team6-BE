package com.deepromeet.atcha.route.infrastructure.cache.config

import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.shared.infrastructure.cache.config.RedisTemplateFactory
import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.infrastructure.cache.TransitInfoMixIn
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class LastRouteRedisConfig {
    @Bean
    fun lastRoutesResponseRedisTemplate(factory: RedisTemplateFactory): RedisTemplate<String, LastRoute> =
        factory.create(LastRoute::class.java) { mapper ->
            mapper.addMixIn(TransitInfo::class.java, TransitInfoMixIn::class.java)
        }
}
