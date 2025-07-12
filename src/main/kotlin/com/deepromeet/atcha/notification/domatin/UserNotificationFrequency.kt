package com.deepromeet.atcha.notification.domatin

enum class UserNotificationFrequency(val minutes: Long) {
    ONE(1),
    FIVE(5),
    TEN(10),
    FIFTEEN(15),
    THIRTY(30),
    SIXTY(60)
    ;

    companion object {
        fun fromMinutes(minute: Int): UserNotificationFrequency =
            entries.find { it.minutes == minute.toLong() }
                ?: throw IllegalArgumentException("Invalid minute value: $minute")
    }
}
