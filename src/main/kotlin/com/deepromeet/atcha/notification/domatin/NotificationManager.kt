package com.deepromeet.atcha.notification.domatin

import com.deepromeet.atcha.notification.infrastructure.fcm.FcmService
import com.deepromeet.atcha.transit.domain.LastRouteReader
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class NotificationManager(
    private val fcmService: FcmService,
    private val lastRouteReader: LastRouteReader,
    private val redisOperations: RouteNotificationRedisOperations
) {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val gson = Gson()
    private val logger = LoggerFactory.getLogger(NotificationManager::class.java)

    fun checkAndNotifyDelay(notification: UserNotification) {
        val minutesDifference =
            calculateMinutesDifference(notification.initialDepartureTime, notification.updatedDepartureTime)

        if (minutesDifference >= 10 && !notification.isDelayNotified) {
            sendPushNotification(notification = notification, isDelay = true)
            redisOperations.updateDelayNotificationFlags(notification)
        }
    }

    private fun calculateMinutesDifference(
        controlTime: String,
        treatmentTime: String
    ): Long {
        val control = LocalDateTime.parse(controlTime, dateTimeFormatter)
        val treatment = LocalDateTime.parse(treatmentTime, dateTimeFormatter)
        return Duration.between(control, treatment).toMinutes()
    }

    fun sendPushNotification(
        notification: UserNotification,
        isDelay: Boolean = false
    ) {
        val json = gson.toJson(lastRouteReader.read(notification.routeId))
        val dataMap: MutableMap<String, String> =
            gson.fromJson(json, object : TypeToken<MutableMap<String, String>>() {}.type)
        dataMap["type"] =
            if (notification.notificationFrequency.minutes.toInt() == 1) {
                "FULL_SCREEN_ALERT"
            } else {
                "PUSH_ALERT"
            }

        val body =
            if (isDelay) {
                createDelayMessage()
            } else {
                createDepartureMessage(notification)
            }

        sendFirebaseMessaging(notification.notificationToken, dataMap, body)
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

        val difference = calculateMinutesDifference(notification.notificationTime, currentMinute)

        return when (notification.notificationFrequency) {
            NotificationFrequency.ONE -> "이제 출발 할 시간이에요. 막차를 타러 가볼까요? \uD83D\uDE8C" // TODO : 임의 추가. 변경 필요
            NotificationFrequency.FIVE ->
                if (difference < NotificationFrequency.FIVE.minutes) {
                    "막차가 예상보다 일찍 출발해요! \uD83D\uDEA8 출발까지 단 ${notification}분!"
                } else {
                    listOf("출발까지 단 5분! 이제 곧 이동할 예정이에요. 인사 나눠요 👋", "⏳ 5분 남았습니다! 출발할 시간이 얼마 남지 않았어요.").random()
                }

            NotificationFrequency.TEN ->
                if (difference < NotificationFrequency.FIVE.minutes) {
                    "\uD83D\uDEA8 출발 시간이 당겨졌어요! 출발까지 ${notification}분 남았어요.\n"
                } else {
                    listOf("10분 전‼\uFE0F 집으로 가는 막차 탈 준비가 되었나요?", "10분 전‼\uFE0F 이젠 진짜 나갈 준비를 슬슬 해야해요.").random()
                }

            NotificationFrequency.FIFTEEN ->
                if (difference < NotificationFrequency.FIVE.minutes) {
                    "\uD83D\uDEA8 막차가 예상보다 일찍 출발해요! ${notification}분 안에 빠르게 준비해요."
                } else {
                    listOf(
                        "출발까지 15분! 슬슬 나갈 준비 해볼까요? \uD83C\uDF92",
                        "15분 남았어요! 자리에서 일어날 준비를 해주세요. \uD83C\uDF92."
                    ).random()
                }

            NotificationFrequency.THIRTY ->
                if (difference < NotificationFrequency.FIVE.minutes) {
                    "⏳ 출발 시간이 당겨졌어요! 최신 막차 정보를 확인해볼까요?"
                } else {
                    listOf("출발까지 30분 남았어요. 막차 경로 확인해볼까요?", "30분 후 출발할 예정이에요. 미리 준비하고 여유롭게 가세요.").random()
                }

            NotificationFrequency.SIXTY ->
                if (difference < NotificationFrequency.FIVE.minutes) {
                    "막차가 예상보다 일찍 출발해요! ${notification}분 동안 이동 계획을 세워볼까요?"
                } else {
                    listOf(
                        "시간이 금이에요! 출발하기까지 1시간 남았어요. 이동 계획을 세워보세요.",
                        "출발까지 1시간 남았어요. 이동 계획을 세워볼까요? \uD83D\uDE0E"
                    ).random()
                }
        }
    }

    private fun sendFirebaseMessaging(
        token: String,
        dataMap: Map<String, String>,
        body: String
    ) {
        logger.info("Sending push notification to $token with body: $body and data: $dataMap")
        fcmService.sendMessageTo(
            targetToken = token,
            title = "앗차",
            body = body,
            data = dataMap
        )
    }
}
