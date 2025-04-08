package com.deepromeet.atcha.notification

import com.deepromeet.atcha.notification.domatin.NotificationService
import com.deepromeet.atcha.support.BaseServiceTest
import com.deepromeet.atcha.support.fixture.LastRouteFixture
import com.deepromeet.atcha.support.fixture.UserFixture
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class NotificationServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var notificationService: NotificationService

    @Test
    fun `경로를 알림으로 등록한다`() {
        // given
        val user = UserFixture.create()
        val lastRoute = LastRouteFixture.create()
        userAppender.save(user)
        lastRouteAppender.append(lastRoute)

        // when
        notificationService.addRouteNotification(user.id, lastRoute.routeId)

        // then
        val userNotifications = userNotificationReader.findById(user.id, lastRoute.routeId)
        org.junit.jupiter.api.Assertions.assertAll(
            { Assertions.assertThat(userNotifications).hasSize(user.alertFrequencies.size) },
            { Assertions.assertThat(userNotifications.get(0).lastRouteId).isEqualTo(lastRoute.routeId) }
        )
        val userNotificationFindByTime = userNotificationReader.findByTime(userNotifications.get(0).notificationTime)
        Assertions.assertThat(userNotificationFindByTime).contains(userNotifications.get(0))
    }

    @Test
    fun `등록된 알림을 삭제한다`() {
        // given
        val user = UserFixture.create(alertFrequencies = mutableSetOf(1))
        val lastRoute = LastRouteFixture.create()
        userAppender.save(user)
        lastRouteAppender.append(lastRoute)
        notificationService.addRouteNotification(user.id, lastRoute.routeId)

        // when
        notificationService.deleteRouteNotification(user.id, lastRoute.routeId)

        // then
        val userNotifications = userNotificationReader.findById(user.id, lastRoute.routeId)
        Assertions.assertThat(userNotifications).hasSize(0)
    }
}
