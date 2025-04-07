package com.deepromeet.atcha.notification.domatin

import org.springframework.stereotype.Component

@Component
class NotificationManager(
    private val routeNotificationOperations: RouteNotificationRedisOperations,
    private val userNotificationAppender: UserNotificationAppender,
    private val userNotificationReader: UserNotificationReader
) {
//
//    private val logger = LoggerFactory.getLogger(NotificationManager::class.java)

//    fun checkAndNotifyDelay(notification: UserNotification) {
//        val minutesDifference =
//            calculateMinutesDifference(notification.initialDepartureTime, notification.updatedDepartureTime)
//
//        if (minutesDifference >= 10 && !notification.isDelayNotified) {
//            sendPushNotification(notification = notification, isDelay = true)
//            routeNotificationOperations.updateDelayNotificationFlags(notification)
//        }
//    }
//
//    fun sendAndDeleteNotification(notification: UserNotification): Boolean {
//        return routeNotificationOperations.handleNotificationWithLock(notification) {
//            if (sendPushNotification(notification)) {
//                userNotificationAppender.deleteUserNotification(notification.userId, notification.lastRouteId)
//                true
//            } else {
//                false
//            }
//        }
//    }
//
//    fun sendPushNotification(
//        notification: UserNotification,
//        isDelay: Boolean = false
//    ): Boolean {
//        userNotificationReader
//        if (!userNotificationReader.hasNotification(notification)) {
//            return false
//        }
//        val dataMap = mutableMapOf<String, String>()
//        dataMap["type"] =
//            if (notification.userNotificationFrequency.minutes.toInt() == 1) {
//                "FULL_SCREEN_ALERT"
//            } else {
//                "PUSH_ALERT"
//            }
//
//        val body =
//            if (isDelay) {
//                createDelayMessage()
//            } else {
//                createDepartureMessage(notification)
//            }
//
//        sendFirebaseMessaging(notification.notificationToken, dataMap, body)
//        return true
//    }
//

//
//
//
//
//
//    private fun sendFirebaseMessaging(
//        token: String,
//        dataMap: MutableMap<String, String>,
//        body: String
//    ) {
//        logger.info("Sending push notification to $token with body: $body and data: $dataMap")
//        val title = "앗차"
//        dataMap["title"] = title
//        dataMap["body"] = body
//        fcmService.send(
//            targetToken = token,
//            title = title,
//            body = body,
//            data = dataMap
//        )
//    }
}
