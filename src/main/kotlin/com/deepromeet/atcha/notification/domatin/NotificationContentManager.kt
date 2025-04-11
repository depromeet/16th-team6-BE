package com.deepromeet.atcha.notification.domatin

import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class NotificationContentManager {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun createPushNotification(notification: UserNotification): NotificationContent {
        val dataMap = getDataMap(notification)
        val body = createDepartureMessage(notification)
        return NotificationContent(body = body, dataMap = dataMap)
    }

    fun createDelayPushNotification(notification: UserNotification): NotificationContent {
        val dataMap = getDataMap(notification)
        val body = createDelayMessage()
        return NotificationContent(body = body, dataMap = dataMap)
    }

    private fun getDataMap(notification: UserNotification): MutableMap<String, String> {
        val dataMap = mutableMapOf<String, String>()
        dataMap["type"] = NotificationType.getByFrequency(notification.userNotificationFrequency).toString()
        return dataMap
    }

    fun createSuggestNotification(): NotificationContent {
        val dataMap = mutableMapOf<String, String>()
        dataMap["type"] = NotificationType.PUSH_ALERT.toString()
        return NotificationContent(
            body = "지금 밖이세요? 막차 알림 등록하고 편히 귀가하세요. \uD83C\uDFE0",
            dataMap = dataMap
        )
    }

    fun createTestNotification(type: String): NotificationContent {
        val dataMap = mutableMapOf<String, String>()
        dataMap["type"] = NotificationType.getByValue(type).toString()

        return NotificationContent(
            body = "지금 밖이세요? 막차 알림 등록하고 편히 귀가하세요. \uD83C\uDFE0",
            dataMap = dataMap
        )
    }

    private fun createDelayMessage(): String =
        listOf(
            "좋은 소식! 막차가 예상보다 늦게 출발해요. 조금 더 머물러도 돼요!",
            "출발 시간이 변경되었어요! 최신 막차 정보를 확인하세요 ✨",
            "좋은 소식 🌷 출발 시간이 조정되었어요. 좀 더 천천히 준비하세요.",
            "조금 더 머물러도 돼요! 막차가 예상보다 늦게 출발해요. 🤗",
            "급할 필요 없어요! 막차 시간이 늦춰졌어요. 지금 최신 정보 확인하기 ↗️"
        ).random()

    private fun createDepartureMessage(notification: UserNotification): String {
        val now = LocalDateTime.now()
        val currentMinute = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        val diffMinutes = calculateDiffMinutes(notification.notificationTime, currentMinute)

        return when (notification.userNotificationFrequency) {
            UserNotificationFrequency.ONE -> "이제 출발 할 시간이에요. 막차를 타러 가볼까요? \uD83D\uDE8C" // TODO : 임의 추가. 변경 필요
            UserNotificationFrequency.FIVE ->
                if (diffMinutes < UserNotificationFrequency.FIVE.minutes) {
                    "막차가 예상보다 일찍 출발해요! \uD83D\uDEA8 출발까지 단 ${notification}분!"
                } else {
                    listOf("출발까지 단 5분! 이제 곧 이동할 예정이에요. 인사 나눠요 👋", "⏳ 5분 남았습니다! 출발할 시간이 얼마 남지 않았어요.").random()
                }

            UserNotificationFrequency.TEN ->
                if (diffMinutes < UserNotificationFrequency.FIVE.minutes) {
                    "\uD83D\uDEA8 출발 시간이 당겨졌어요! 출발까지 ${notification}분 남았어요.\n"
                } else {
                    listOf("10분 전‼\uFE0F 집으로 가는 막차 탈 준비가 되었나요?", "10분 전‼\uFE0F 이젠 진짜 나갈 준비를 슬슬 해야해요.").random()
                }

            UserNotificationFrequency.FIFTEEN ->
                if (diffMinutes < UserNotificationFrequency.FIVE.minutes) {
                    "\uD83D\uDEA8 막차가 예상보다 일찍 출발해요! ${notification}분 안에 빠르게 준비해요."
                } else {
                    listOf(
                        "출발까지 15분! 슬슬 나갈 준비 해볼까요? \uD83C\uDF92",
                        "15분 남았어요! 자리에서 일어날 준비를 해주세요. \uD83C\uDF92."
                    ).random()
                }

            UserNotificationFrequency.THIRTY ->
                if (diffMinutes < UserNotificationFrequency.FIVE.minutes) {
                    "⏳ 출발 시간이 당겨졌어요! 최신 막차 정보를 확인해볼까요?"
                } else {
                    listOf("출발까지 30분 남았어요. 막차 경로 확인해볼까요?", "30분 후 출발할 예정이에요. 미리 준비하고 여유롭게 가세요.").random()
                }

            UserNotificationFrequency.SIXTY ->
                if (diffMinutes < UserNotificationFrequency.FIVE.minutes) {
                    "막차가 예상보다 일찍 출발해요! ${notification}분 동안 이동 계획을 세워볼까요?"
                } else {
                    listOf(
                        "시간이 금이에요! 출발하기까지 1시간 남았어요. 이동 계획을 세워보세요.",
                        "출발까지 1시간 남았어요. 이동 계획을 세워볼까요? \uD83D\uDE0E"
                    ).random()
                }
        }
    }

    private fun calculateDiffMinutes(
        controlTime: String,
        treatmentTime: String
    ): Long {
        val control = LocalDateTime.parse(controlTime, dateTimeFormatter)
        val treatment = LocalDateTime.parse(treatmentTime, dateTimeFormatter)
        return Duration.between(control, treatment).toMinutes()
    }
}
