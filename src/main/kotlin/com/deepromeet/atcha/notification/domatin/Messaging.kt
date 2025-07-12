package com.deepromeet.atcha.notification.domatin

data class Messaging(
    val token: String,
    val title: String,
    val body: String,
    val dataMap: Map<String, String> = emptyMap()
) {
    constructor(notificationContent: NotificationContent, token: String) : this(
        token = token,
        title = notificationContent.title,
        body = notificationContent.body,
        dataMap = notificationContent.dataMap
    )
}
