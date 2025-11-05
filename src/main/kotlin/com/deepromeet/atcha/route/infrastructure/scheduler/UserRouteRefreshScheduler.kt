package com.deepromeet.atcha.route.infrastructure.scheduler

import com.deepromeet.atcha.route.application.UserRouteRefreshService
import kotlinx.coroutines.runBlocking
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UserRouteRefreshScheduler(
    private val userRouteRefreshService: UserRouteRefreshService
) {
    @Scheduled(cron = "0 0/2 * * * ?")
    @SchedulerLock(name = "refresh_departure_time", lockAtMostFor = "PT5S", lockAtLeastFor = "PT3S")
    fun processRefresh() =
        runBlocking {
            userRouteRefreshService.refreshAllAndPublishEvents()
        }
}
