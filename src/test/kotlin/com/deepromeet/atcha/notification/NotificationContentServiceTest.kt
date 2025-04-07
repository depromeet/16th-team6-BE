package com.deepromeet.atcha.notification

import com.deepromeet.atcha.notification.domatin.NotificationService
import com.deepromeet.atcha.support.BaseServiceTest
import com.deepromeet.atcha.support.fixture.LastRouteFixture
import com.deepromeet.atcha.support.fixture.UserFixture
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class NotificationContentServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var notificationService: NotificationService

    @Test
    fun `사용자의 알림 빈도만큼 알림을 저장한다`() {
        // given
        val user = userAppender.save(UserFixture.create())
        val lastRoute = LastRouteFixture.create()
        lastRouteAppender.append(lastRoute)

        // when
        notificationService.addRouteNotification(user.id, lastRoute.routeId)

        // then
        val result = userNotificationReader.findById(user.id, lastRoute.routeId)
        Assertions.assertThat(result).hasSize(user.alertFrequencies.size)
    }

    @Test
    fun `사용자의 알림을 삭제한다`() {
        // given
        val user = userAppender.save(UserFixture.create())
        val lastRoute = LastRouteFixture.create()
        lastRouteAppender.append(lastRoute)
        notificationService.addRouteNotification(user.id, lastRoute.routeId)
        var result = userNotificationReader.findById(user.id, lastRoute.routeId)
        Assertions.assertThat(result.size).isGreaterThan(0)

        // when
        notificationService.deleteRouteNotification(user.id, lastRoute.routeId)

        // then
        result = userNotificationReader.findById(user.id, lastRoute.routeId)
        Assertions.assertThat(result.size).isEqualTo(0)
    }
}
