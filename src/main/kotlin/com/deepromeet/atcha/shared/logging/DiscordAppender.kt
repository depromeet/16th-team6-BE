package com.deepromeet.atcha.shared.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import ch.qos.logback.core.UnsynchronizedAppenderBase
import com.deepromeet.atcha.shared.infrastructure.discord.DiscordMessage
import com.deepromeet.atcha.shared.infrastructure.discord.DiscordMessage.Embed
import com.deepromeet.atcha.shared.logging.exception.DiscordError
import com.deepromeet.atcha.shared.logging.exception.DiscordException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.net.URL
import java.time.LocalDateTime
import javax.net.ssl.HttpsURLConnection

class DiscordAppender(
    var webhookUrl: String = "",
    var username: String = ""
) : UnsynchronizedAppenderBase<ILoggingEvent>() {
    companion object {
        private const val CAUSED_BY = "Caused by:"
        private const val CONTENT_TYPE = "Content-Type"

        private const val CONNECT_TIMEOUT_MS = 2000
        private const val READ_TIMEOUT_MS = 2000

        private const val LOG_MAX_LEN = 1900
    }

    private val objectMapper = ObjectMapper()

    override fun append(event: ILoggingEvent?) {
        if (event == null || webhookUrl.isBlank()) return

        try {
            val url: URL = URL(webhookUrl)
            val connection =
                (url.openConnection() as HttpsURLConnection).apply {
                    requestMethod = HttpMethod.POST.name()
                    setRequestProperty(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    doOutput = true
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                }

            connection.getOutputStream().use { stream ->
                stream.write(createMessage(event))
                stream.flush()

                connection.getInputStream().close()
                connection.disconnect()
            }
        } catch (e: Exception) {
            throw DiscordException.of(DiscordError.DISCORD_LOG_DELIVERY_FAILURE, e)
        }
    }

    private fun createMessage(event: ILoggingEvent?): ByteArray {
        return objectMapper.writeValueAsBytes(
            DiscordMessage().apply {
                content = "🚨 에러 발생 비이이이이사아아아앙"
                embeds =
                    listOf(
                        Embed().apply {
                            title = "ℹ️ 에러 정보"
                            description =
                                """
                                🕖 발생 시간 : ${LocalDateTime.now()}
                                📄 예외 : ${getStackTrace(event)}
                                """.trimIndent()
                        }
                    )
            }
        )
    }

    private fun getStackTrace(event: ILoggingEvent?): String {
        if (event == null) {
            return "로그 정보가 소실되었습니다."
        }

        var message = "[${event.level}] - ${event.formattedMessage}"

        val throwableProxy = event.throwableProxy
        if (throwableProxy != null) {
            val stackTrace = ThrowableProxyUtil.asString(throwableProxy)

            // Root Cause를 찾기 위해 "Caused by:" 라인들을 모두 찾아서 가장 마지막 것을 사용
            val causedByLines = stackTrace.lines().filter { it.trim().startsWith(CAUSED_BY) }

            if (causedByLines.isNotEmpty()) {
                // 가장 마지막 "Caused by:"가 실제 root cause
                val rootCause = causedByLines.last().trim()
                val rootCauseException = rootCause.substring(CAUSED_BY.length).trim()
                message += "\n🔍 Root Cause: $rootCauseException"
            } else {
                // "Caused by:"가 없으면 첫 번째 예외가 root cause
                val firstLine = stackTrace.lines().firstOrNull { it.trim().isNotEmpty() }
                if (firstLine != null) {
                    message += "\n🔍 Exception: ${firstLine.trim()}"
                }
            }
        }

        return message.take(LOG_MAX_LEN)
    }
}
