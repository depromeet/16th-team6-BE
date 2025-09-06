package com.deepromeet.atcha.shared.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.UnsynchronizedAppenderBase
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class DiscordAppender(
    val webhookUrl: String,
    val username: String,
) : UnsynchronizedAppenderBase<ILoggingEvent>() {
    override fun append(p0: ILoggingEvent?) {

        println("여기 실행됨!!!!------------------------------------------------------------------------------------------------")
        val url: URL = URL(webhookUrl)
        val connection = url.openConnection() as HttpsURLConnection
        connection.addRequestProperty("Content-Type", "application/json")
        connection.addRequestProperty("User-Agent", "Java-DiscordWebhook-BY-Gelox_")
        connection.setDoOutput(true)
        connection.setRequestMethod("POST")

        try {
            connection.getOutputStream().use { stream ->
                stream.write("오류 발생!!".toByteArray())
                stream.flush()

                connection.getInputStream().close()
                connection.disconnect()
            }
        } catch (ioException: IOException) {
            throw ioException
        }
    }
}
