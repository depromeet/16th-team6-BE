package com.deepromeet.atcha.shared.domain.event.infrastructure

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.PendingMessage
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

private val log = KotlinLogging.logger {}

@Component
class RedisStreamPendingMessageReclaimer(
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val streamOps = redisTemplate.opsForStream<String, String>()

    fun reclaimPendingMessages(
        streamKey: String,
        groupName: String,
        consumerId: String,
        onMessageReclaimed: (MapRecord<String, String, String>) -> EventHandleResult
    ) {
        val reclaimRecordIds = getPendingMessagesToReclaim(streamKey, groupName)
        if (reclaimRecordIds.isEmpty()) return

        val claimedMessages = claimMessages(streamKey, groupName, consumerId, reclaimRecordIds)

        claimedMessages.forEach { message ->
            val result = onMessageReclaimed(message)
            handleReclaimResult(result, message, streamKey, groupName)
        }
    }

    private fun getPendingMessagesToReclaim(
        streamKey: String,
        groupName: String
    ): List<RecordId> {
        val pendingMessages =
            streamOps.pending(
                streamKey,
                groupName,
                Range.unbounded<RecordId>(),
                100
            )
        return pendingMessages
            .filter { isReclaimTarget(it) }
            .map { it.id }
            .toList()
    }

    private fun claimMessages(
        streamKey: String,
        groupName: String,
        consumerId: String,
        reclaimRecordIds: List<RecordId>
    ): List<MapRecord<String, String, String>> {
        return reclaimRecordIds.flatMap { recordId ->
            streamOps.claim(streamKey, groupName, consumerId, Duration.ofMinutes(3), recordId)
        }
    }

    private fun handleReclaimResult(
        result: EventHandleResult,
        message: MapRecord<String, String, String>,
        streamKey: String,
        groupName: String
    ) {
        when (result.status) {
            EventHandleResult.Status.SUCCESS -> {
                streamOps.acknowledge(streamKey, groupName, message.id)
                log.info { "재처리된 메시지 처리 성공: ${message.id}" }
            }
            EventHandleResult.Status.FAILED,
            EventHandleResult.Status.NO_HANDLER -> {
                // Dead Letter 처리는 호출하는 쪽에서 담당
                streamOps.acknowledge(streamKey, groupName, message.id)
                log.warn { "재처리된 메시지 처리 실패: ${message.id}" }
            }
        }
    }

    private fun isReclaimTarget(message: PendingMessage): Boolean =
        message.totalDeliveryCount > 1 &&
            message.elapsedTimeSinceLastDelivery > Duration.ofMinutes(3)
}
