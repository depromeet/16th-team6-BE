package com.deepromeet.atcha.notification.domatin

data class NotificationContent(
    val title: String = "앗차",
    val body: String,
    val dataMap: Map<String, String> = emptyMap()
)
