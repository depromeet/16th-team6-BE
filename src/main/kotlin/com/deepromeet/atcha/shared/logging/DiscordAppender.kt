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
    var username: String = "",
) : UnsynchronizedAppenderBase<ILoggingEvent>() {

    private val CAUSED_BY = "Caused by:"
    private val CONTENT_TYPE = "Content-Type"

    private val objectMapper = ObjectMapper()

    override fun append(event: ILoggingEvent?) {

        try {
            val url: URL = URL(webhookUrl)
            val connection = (url.openConnection() as HttpsURLConnection).apply {
                requestMethod = HttpMethod.POST.name()
                setRequestProperty(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                doOutput = true
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
            DiscordMessage().apply{
                content = "# 🚨 에러 발생 비이이이이사아아아앙"
                embeds = listOf(
                    Embed().apply {
                        title = "ℹ️ 에러 정보"
                        description = """
                            🕖 발생 시간 : ${LocalDateTime.now()}
                            📄 예외 : ${getStackTrace(event)}
                        """.trimIndent()
                    }
                )
            }
        )
    }

    private fun getStackTrace(event: ILoggingEvent?): String {
        if(event == null) return "로그 정보가 소실되었습니다."
        val message = "[${event.level}] ${event.loggerName} - ${event.formattedMessage}".take(1900)

        val throwableProxy = event.throwableProxy

        if (throwableProxy != null) {
            val stackTrace = ThrowableProxyUtil.asString(throwableProxy)
            println("stackTrace = $stackTrace")
            var causedBy = stackTrace.lines().firstOrNull { it.contains(CAUSED_BY) }
                .toString()
            if(causedBy == null) return "로그 정보(causedBy)가 소실되었습니다."

            val causedByIndex = causedBy.indexOf(CAUSED_BY)

            causedBy = causedBy.substring(causedByIndex + 10)
            return causedBy.take(1900)
        }

        // 예외 정보가 없으면 기존 메시지만 반환
        return message
    }
}
