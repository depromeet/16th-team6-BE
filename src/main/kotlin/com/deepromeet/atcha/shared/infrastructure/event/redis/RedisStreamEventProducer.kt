package com.deepromeet.atcha.shared.infrastructure.event.redis

import com.deepromeet.atcha.shared.domain.event.domain.DomainEvent
import com.deepromeet.atcha.shared.domain.event.domain.EventBus
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

private const val EVENT_TYPE = "eventType"
private const val EVENT_ID = "eventId"
private const val PAYLOAD = "payload"
private const val OCCURRED_AT = "occurredAt"
private const val AGGREGATE_ID = "aggregateId"

private val log = KotlinLogging.logger {}

@Component
class RedisStreamEventProducer(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${redis.stream.event.key}")
    private val streamKey: String
) : EventBus {
    private val streamOps = redisTemplate.opsForStream<String, String>()

    override fun publish(event: DomainEvent) {
        val record =
            StreamRecords.newRecord()
                .ofMap(createEventMap(event))
                .withStreamKey(streamKey)

        streamOps.add(record)
    }

    override fun publishAll(events: List<DomainEvent>) {
        if (events.isEmpty()) return

        val records =
            events.map { event ->
                log.debug { "📤 이벤트 발송 - 타입: ${event.eventType}, 집계ID: ${event.aggregateId}" }

                StreamRecords.newRecord()
                    .ofMap(createEventMap(event))
                    .withStreamKey(streamKey)
            }

        try {
            redisTemplate.executePipelined { connection ->
                records.forEach { record ->
                    streamOps.add(record)
                }
                null
            }
            log.info { "✅ 도메인 이벤트 ${events.size}개 발송 완료" }
        } catch (e: Exception) {
            log.error(e) { "❌ 도메인 이벤트 발송 실패: ${events.map { "${it.eventType}(${it.aggregateId})" }}" }
            throw e
        }
    }

    private fun createEventMap(event: DomainEvent): Map<String, String> {
        return mapOf(
            EVENT_TYPE to event.eventType,
            EVENT_ID to event.eventId,
            PAYLOAD to objectMapper.writeValueAsString(event),
            OCCURRED_AT to event.occurredAt.toString(),
            AGGREGATE_ID to event.aggregateId
        )
    }
}
