package com.deepromeet.atcha.app.domain

enum class Platform(val displayName: String) {
    ANDROID("Android"),
    IOS("iOS");

    companion object {
        fun fromString(value: String): Platform {
            return entries.find {
                it.displayName.equals(value, ignoreCase = true)
            } ?: throw IllegalArgumentException("알수없는 플랫폼입니다: $value")
        }
    }
}
