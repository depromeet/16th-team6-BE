package com.deepromeet.atcha.userroute.infrastructure.redis

import com.deepromeet.atcha.userroute.domain.UserRoute
import com.deepromeet.atcha.userroute.domain.UserRouteProducer
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

private const val PAYLOAD = "payload"
private const val RETRY_COUNT = "retryCount"

@Component
class RedisUserRouteProducer(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${redis.stream.notification.key}")
    private val key: String,
    @Value("\${redis.stream.notification.group}")
    private val group: String,
    @Value("\${redis.stream.notification.dead-letter.key}")
    private val deadLetterKey: String
) : UserRouteProducer {
    private val streamOps = redisTemplate.opsForStream<String, String>()

    override fun produce(
        userRoute: UserRoute,
        retryCount: Int
    ) {
        val record =
            StreamRecords.newRecord()
                .`in`(group)
                .ofMap(
                    mapOf(
                        PAYLOAD to objectMapper.writeValueAsString(userRoute),
                        RETRY_COUNT to retryCount.toString()
                    )
                )
                .withStreamKey(key)
        streamOps.add(record)
    }

    override fun produceAll(userRoutes: List<UserRoute>) {
        if (userRoutes.isEmpty()) return

        // Redis Pipeline을 활용한 배치 처리
        val records =
            userRoutes.map { userLastRoute ->
                StreamRecords.newRecord()
                    .`in`(group)
                    .ofMap(
                        mapOf(
                            PAYLOAD to objectMapper.writeValueAsString(userLastRoute),
                            RETRY_COUNT to "0"
                        )
                    )
                    .withStreamKey(key)
            }

        // 한 번의 네트워크 호출로 모든 레코드 추가
        redisTemplate.executePipelined { connection ->
            records.forEach { record ->
                streamOps.add(record)
            }
            null
        }
    }

    override fun produceToDeadLetter(
        userRoute: UserRoute,
        retryCount: Int
    ) {
        val record =
            StreamRecords.newRecord()
                .ofMap(
                    mapOf(
                        PAYLOAD to objectMapper.writeValueAsString(userRoute),
                        RETRY_COUNT to retryCount.toString()
                    )
                )
                .withStreamKey(deadLetterKey)
        streamOps.add(record)
    }
}
