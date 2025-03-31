package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.transit.domain.HolidayCache
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class HolidayRedisCache(
    private val holidayRedisTemplate: RedisTemplate<String, List<LocalDate>>
) : HolidayCache {
    override fun cacheHolidays(holidays: List<LocalDate>) {
        val key = getKey(holidays.first().year)
        holidayRedisTemplate.opsForValue().set(key, holidays)
    }

    override fun getHolidays(year: Int): List<LocalDate>? {
        val key = getKey(year)
        return holidayRedisTemplate.opsForValue().get(key)
    }

    private fun getKey(year: Int): String {
        return "holidays:$year"
    }
}
