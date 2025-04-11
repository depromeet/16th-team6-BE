package com.deepromeet.atcha.notification.domatin

enum class NotificationType(
    private val value: Int
) {
    FULL_SCREEN_ALERT(1),
    PUSH_ALERT(2);

    companion object {
        fun getByValue(value: String): NotificationType =
            entries.find { it.value == value.toInt() }
                ?: FULL_SCREEN_ALERT
    }
}
