package com.deepromeet.atcha.shared.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import ch.qos.logback.core.UnsynchronizedAppenderBase
import com.deepromeet.atcha.shared.infrastructure.discord.DiscordMessage
import com.deepromeet.atcha.shared.infrastructure.discord.DiscordMessage.Embed
import com.deepromeet.atcha.shared.logging.exception.DiscordError
import com.deepromeet.atcha.shared.logging.exception.DiscordException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.net.URL
import java.time.LocalDateTime
import javax.net.ssl.HttpsURLConnection

@Profile("dev", "prod")
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
        var message: String?

        if (event == null) message = "로그 정보가 소실되었습니다."
        else {
            message = "[${event.level}] ${event.loggerName} - ${event.formattedMessage}".take(LOG_MAX_LEN)

            val throwableProxy = event.throwableProxy

            if (throwableProxy != null) {
                val stackTrace = ThrowableProxyUtil.asString(throwableProxy)
                println("stackTrace = $stackTrace")
                val causedBy =
                    stackTrace.lines().firstOrNull { it.contains(CAUSED_BY) }
                        .toString()
                if (causedBy == null) message = stackTrace.toString().take(LOG_MAX_LEN)

                val causedByIndex = causedBy.indexOf(CAUSED_BY)

                message = causedBy.substring(causedByIndex + 10)
            }
        }

        // 예외 정보가 없으면 기존 메시지만 반환
        return message.take(LOG_MAX_LEN)
    }
}
