package com.deepromeet.atcha.notification.domatin

enum class NotificationType(
    private val value: Int,
    private val frequencies: List<UserNotificationFrequency>
) {
    FULL_SCREEN_ALERT(1, listOf(UserNotificationFrequency.ONE)),
    PUSH_ALERT(2, UserNotificationFrequency.entries.filter { it != UserNotificationFrequency.ONE })
    ;

    companion object {
        fun getByValue(value: String): NotificationType =
            entries.find { it.value == value.toInt() }
                ?: FULL_SCREEN_ALERT

        fun getByFrequency(frequency: UserNotificationFrequency): NotificationType =
            entries.find { it.frequencies.contains(frequency) }
                ?: PUSH_ALERT
    }
}
