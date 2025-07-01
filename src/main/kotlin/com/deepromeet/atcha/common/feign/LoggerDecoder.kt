package com.deepromeet.atcha.common.feign

import com.deepromeet.atcha.common.utils.PrettyBodyFormatter
import feign.Response
import feign.codec.Decoder
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.reflect.Type

private val log = KotlinLogging.logger {}

class LoggerDecoder(
    private val delegate: Decoder
) : Decoder {
    override fun decode(
        response: Response,
        type: Type
    ): Any? {
        val bodyBytes = response.body()?.asInputStream()?.readAllBytes() ?: ByteArray(0)
        val buffered = response.toBuilder().body(bodyBytes).build()

        return try {
            delegate.decode(buffered, type)
        } catch (e: Exception) {
            val pretty = PrettyBodyFormatter.format(bodyBytes, response.headers())
            log.warn(e) { "응답 디코딩 실패\n=== Response Body ===\n$pretty\n====================" }
            throw e
        }
    }
}
