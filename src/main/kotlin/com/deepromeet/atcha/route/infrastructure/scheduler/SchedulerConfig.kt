package com.deepromeet.atcha.route.infrastructure.scheduler

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
class SchedulerConfig : SchedulingConfigurer {
    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.setPoolSize(10)
        scheduler.setThreadNamePrefix("scheduled-task-pool-")
        scheduler.initialize()
        taskRegistrar.setTaskScheduler(scheduler)
    }

    @Bean
    fun lockProvider(redisConnectionFactory: RedisConnectionFactory): LockProvider {
        return RedisLockProvider(redisConnectionFactory)
    }
}
