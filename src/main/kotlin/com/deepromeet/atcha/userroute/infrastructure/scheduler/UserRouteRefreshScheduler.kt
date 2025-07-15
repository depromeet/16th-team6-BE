package com.deepromeet.atcha.userroute.infrastructure.scheduler

import com.deepromeet.atcha.transit.domain.route.LastRouteDepartureTimeRefresher
import com.deepromeet.atcha.userroute.domain.UserRouteProducer
import kotlinx.coroutines.runBlocking
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UserRouteRefreshScheduler(
    private val lastRouteDepartureTimeRefresher: LastRouteDepartureTimeRefresher,
    private val userRouteProducer: UserRouteProducer
) {
    @Scheduled(cron = "0 * * * * ?")
    @SchedulerLock(name = "refresh_departure_time", lockAtMostFor = "PT2S", lockAtLeastFor = "PT2S")
    fun processRefresh() =
        runBlocking {
            val refreshedRoutes = lastRouteDepartureTimeRefresher.refreshAll()
            userRouteProducer.produceAll(refreshedRoutes)
        }
}
