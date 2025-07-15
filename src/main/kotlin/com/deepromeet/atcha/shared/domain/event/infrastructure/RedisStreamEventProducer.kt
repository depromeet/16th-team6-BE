package com.deepromeet.atcha.shared.domain.event.infrastructure

import com.deepromeet.atcha.shared.domain.event.domain.DomainEvent
import com.deepromeet.atcha.shared.domain.event.domain.EventBus
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

private const val EVENT_TYPE = "eventType"
private const val EVENT_ID = "eventId"
private const val PAYLOAD = "payload"
private const val OCCURRED_AT = "occurredAt"
private const val AGGREGATE_ID = "aggregateId"

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
                StreamRecords.newRecord()
                    .ofMap(createEventMap(event))
                    .withStreamKey(streamKey)
            }

        redisTemplate.executePipelined { connection ->
            records.forEach { record ->
                streamOps.add(record)
            }
            null
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
