package com.deepromeet.atcha.user.domain

@JvmInline
value class Email(val value: String) {

    init {
        require(value.matches(EMAIL_REGEX)) { "Invalid email format: $value" }
    }

    companion object {
        private val EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$".toRegex()

        fun from(value: String): Email = Email(value)
    }
}