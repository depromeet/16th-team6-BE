package com.deepromeet.atcha.shared.infrastructure.event.redis

import com.deepromeet.atcha.shared.domain.event.domain.EventHandler
import com.deepromeet.atcha.shared.domain.event.exception.EventError
import com.deepromeet.atcha.shared.domain.event.exception.EventException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.connection.stream.StreamReadOptions
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.time.Duration

private const val EVENT_TYPE = "eventType"
private const val PAYLOAD = "payload"
private val log = KotlinLogging.logger {}

@Service
class RedisStreamEventConsumer(
    private val eventHandlers: List<EventHandler>,
    private val pendingMessageReclaimer: RedisStreamPendingMessageReclaimer,
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${redis.stream.event.key}")
    private val streamKey: String,
    @Value("\${redis.stream.event.group}")
    private val groupName: String,
    @Value("\${redis.stream.event.dead-letter.key}")
    private val deadLetterKey: String
) {
    private val streamOps = redisTemplate.opsForStream<String, String>()
    private val consumerId: String = InetAddress.getLocalHost().hostName
    private val consumer = Consumer.from(groupName, consumerId)
    private val readOptions = StreamReadOptions.empty().block(Duration.ofSeconds(1))
    private val streams = StreamOffset.create(streamKey, ReadOffset.lastConsumed())

    @Scheduled(cron = "*/10 * * * * ?")
    fun consumeEvents() {
        val messages = streamOps.read(consumer, readOptions, streams)

        messages?.forEach { message ->
            val result = handleEvent(message)
            handleEventResult(result, message)
        }
    }

    @Scheduled(cron = "*/10 * * * * ?")
    fun reclaimPendingEvents() {
        pendingMessageReclaimer.reclaimPendingMessages(
            streamKey = streamKey,
            groupName = groupName,
            consumerId = consumerId
        ) { message ->
            val result = handleEvent(message)
            if (result.status == EventHandleResult.Status.FAILED ||
                result.status == EventHandleResult.Status.NO_HANDLER
            ) {
                moveToDeadLetter(message)
            }
            result
        }
    }

    private fun handleEvent(message: MapRecord<String, String, String>): EventHandleResult {
        val eventType =
            message.value[EVENT_TYPE]
                ?: throw EventException.of(
                    EventError.INVALID_EVENT,
                    "$EVENT_TYPE 컬럼 데이터가 없습니다."
                )

        val payload =
            message.value[PAYLOAD]
                ?: throw EventException.of(
                    EventError.INVALID_EVENT,
                    "$PAYLOAD 컬럼 데이터가 없습니다."
                )

        val handler =
            eventHandlers.find { it.supports(eventType) }
                ?: return EventHandleResult.noHandler(message.id, eventType)

        return try {
            handler.handle(payload)
            EventHandleResult.success(message.id)
        } catch (e: Exception) {
            log.error(e) { "이벤트 처리 실패: ${message.id}" }
            EventHandleResult.failed(message.id, e)
        }
    }

    private fun handleEventResult(
        result: EventHandleResult,
        message: MapRecord<String, String, String>
    ) {
        when (result.status) {
            EventHandleResult.Status.SUCCESS -> {
                streamOps.acknowledge(streamKey, groupName, message.id)
            }
            EventHandleResult.Status.FAILED,
            EventHandleResult.Status.NO_HANDLER -> {
                moveToDeadLetter(message)
                streamOps.acknowledge(streamKey, groupName, message.id)
            }
        }
    }

    private fun moveToDeadLetter(message: MapRecord<String, String, String>) {
        streamOps.add(deadLetterKey, message.value)
    }
}

data class EventHandleResult(
    val status: Status,
    val messageId: RecordId,
    val error: Exception? = null,
    val eventType: String? = null
) {
    enum class Status { SUCCESS, FAILED, NO_HANDLER }

    companion object {
        fun success(id: RecordId) = EventHandleResult(Status.SUCCESS, id)

        fun failed(
            id: RecordId,
            error: Exception
        ) = EventHandleResult(Status.FAILED, id, error)

        fun noHandler(
            id: RecordId,
            eventType: String?
        ) = EventHandleResult(
            Status.NO_HANDLER,
            id,
            eventType = eventType
        )
    }
}
