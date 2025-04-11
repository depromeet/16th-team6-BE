package com.deepromeet.atcha.notification.domatin

data class Messaging(
    val token: String,
    val title: String,
    val body: String,
    val dataMap: Map<String, String> = emptyMap()
) {
    constructor(notificationContent: NotificationContent, token: String) : this(
        notificationContent.title,
        notificationContent.body,
        token,
        notificationContent.dataMap
    )
}
