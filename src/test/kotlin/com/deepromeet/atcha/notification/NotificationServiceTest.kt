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
    fun `알림을 추가한다`() {
        // given
        val user = UserFixture.create(alertFrequencies = 1L)
        val lastRoute = LastRouteFixture.create()
        userAppender.save(user)
        lastRouteAppender.append(lastRoute)

        // when
        notificationService.addRouteNotification(user.id, lastRoute.routeId)

        // then
        Assertions.assertThat(userNotificationReader.findById(user.id, lastRoute.routeId)).isEqualTo(lastRoute)
    }
}
