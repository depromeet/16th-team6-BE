package com.deepromeet.atcha.userroute.infrastructure.scheduler

import com.deepromeet.atcha.userroute.application.UserRouteRefreshService
import kotlinx.coroutines.runBlocking
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UserRouteRefreshScheduler(
    private val userRouteRefreshService: UserRouteRefreshService
) {
    @Scheduled(cron = "0 * * * * ?")
    @SchedulerLock(name = "refresh_departure_time", lockAtMostFor = "PT2S", lockAtLeastFor = "PT2S")
    fun processRefresh() =
        runBlocking {
            userRouteRefreshService.refreshAllAndPublishEvents()
        }
}
