package com.deepromeet.atcha.notification.domatin

enum class NotificationFrequency(val minutes: Long) {
    FIVE(5),
    TEN(10),
    FIFTEEN(15),
    THIRTY(30),
    SIXTY(60)
    ;

    companion object {
        fun fromMinutes(minute: Int): NotificationFrequency =
            entries.find { it.minutes == minute.toLong() }
                ?: throw IllegalArgumentException("Invalid minute value: $minute")
    }
}
