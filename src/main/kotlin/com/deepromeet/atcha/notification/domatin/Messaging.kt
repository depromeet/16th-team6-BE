package com.deepromeet.atcha.notification.domatin

data class Messaging(
    val title: String,
    val body: String,
    val token: String,
    val dataMap: Map<String, String> = emptyMap()
) {
    constructor(notification: Notification, token: String) : this(
        notification.title,
        notification.body,
        token,
        notification.dataMap
    )
}
