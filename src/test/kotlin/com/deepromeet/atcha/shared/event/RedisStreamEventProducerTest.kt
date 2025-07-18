package com.deepromeet.atcha.shared.event

import com.deepromeet.atcha.shared.domain.event.domain.DomainEvent
import com.deepromeet.atcha.shared.domain.event.domain.EventBus
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.TestPropertySource
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
@TestPropertySource(properties = ["redis.stream.event.key=test:events"])
class RedisStreamEventProducerTest {
    @Autowired
    private lateinit var eventBus: EventBus

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val testStreamKey = "test:events"

    @BeforeEach
    fun setUp() {
        // 테스트 전 Stream 초기화
        redisTemplate.delete(testStreamKey)
    }

    @AfterEach
    fun tearDown() {
        // 테스트 후 Stream 정리
        redisTemplate.delete(testStreamKey)
    }

    @Test
    fun `단일 이벤트 발행 테스트`() {
        // Given
        val testEvent =
            TestDomainEvent(
                eventId = UUID.randomUUID().toString(),
                aggregateId = "user-123",
                testData = "단일 이벤트 테스트"
            )

        // When
        eventBus.publish(testEvent)

        // Then
        val streamOps = redisTemplate.opsForStream<String, String>()
        val messages = streamOps.read(StreamOffset.fromStart(testStreamKey))

        // 메시지가 발행되었는지 검증
        assertThat(messages).isNotNull
        assertThat(messages).hasSize(1)

        val message = messages!!.first()
        assertThat(message.value).containsKeys("eventType", "eventId", "aggregateId", "occurredAt", "payload")

        // 이벤트 타입 검증
        assertThat(message.value["eventType"]).isEqualTo("TEST_EVENT")
        assertThat(message.value["eventId"]).isEqualTo(testEvent.eventId)
        assertThat(message.value["aggregateId"]).isEqualTo(testEvent.aggregateId)

        // Payload JSON 검증
        val payload = message.value["payload"]
        assertThat(payload).isNotBlank

        // JSON 파싱이 정상적으로 되는지 검증
        val parsedEvent = objectMapper.readValue(payload, TestDomainEvent::class.java)
        assertThat(parsedEvent.eventId).isEqualTo(testEvent.eventId)
        assertThat(parsedEvent.aggregateId).isEqualTo(testEvent.aggregateId)
        assertThat(parsedEvent.testData).isEqualTo(testEvent.testData)
    }

    @Test
    fun `여러 이벤트 배치 발행 테스트`() {
        // Given
        val events =
            listOf(
                TestDomainEvent(
                    eventId = UUID.randomUUID().toString(),
                    aggregateId = "user-123",
                    testData = "배치 이벤트 1"
                ),
                TestDomainEvent(
                    eventId = UUID.randomUUID().toString(),
                    aggregateId = "user-456",
                    testData = "배치 이벤트 2"
                ),
                TestDomainEvent(
                    eventId = UUID.randomUUID().toString(),
                    aggregateId = "user-789",
                    testData = "배치 이벤트 3"
                )
            )

        // When
        eventBus.publishAll(events)

        // Then
        val streamOps = redisTemplate.opsForStream<String, String>()
        val messages = streamOps.read(StreamOffset.fromStart(testStreamKey))

        // 모든 이벤트가 발행되었는지 검증
        assertThat(messages).isNotNull
        assertThat(messages).hasSize(3)

        // 각 이벤트의 데이터 검증
        val messageValues = messages!!.map { it.value }
        val eventIds = events.map { it.eventId }
        val aggregateIds = events.map { it.aggregateId }

        messageValues.forEach { messageValue ->
            assertThat(messageValue).containsKeys("eventType", "eventId", "aggregateId", "occurredAt", "payload")
            assertThat(messageValue["eventType"]).isEqualTo("TEST_EVENT")
            assertThat(eventIds).contains(messageValue["eventId"])
            assertThat(aggregateIds).contains(messageValue["aggregateId"])

            // Payload 검증
            val payload = messageValue["payload"]
            assertThat(payload).isNotBlank

            // JSON 파싱 검증
            val parsedEvent = objectMapper.readValue(payload, TestDomainEvent::class.java)
            assertThat(parsedEvent.eventType).isEqualTo("TEST_EVENT")
            assertThat(parsedEvent.testData).startsWith("배치 이벤트")
        }
    }

    @Test
    fun `빈 이벤트 리스트 발행 시 아무것도 발행되지 않는다`() {
        // Given
        val emptyEvents = emptyList<TestDomainEvent>()

        // When
        eventBus.publishAll(emptyEvents)

        // Then
        val streamOps = redisTemplate.opsForStream<String, String>()

        // Stream이 존재하지 않아야 함
        val streamExists = redisTemplate.hasKey(testStreamKey)
        assertThat(streamExists).isFalse
    }

    @Test
    fun `이벤트 발행 순서가 유지되는지 검증`() {
        // Given
        val orderedEvents =
            (1..5).map { index ->
                TestDomainEvent(
                    eventId = "event-$index",
                    aggregateId = "user-$index",
                    testData = "순서 테스트 $index"
                )
            }

        // When
        eventBus.publishAll(orderedEvents)

        // Then
        val streamOps = redisTemplate.opsForStream<String, String>()
        val messages = streamOps.read(StreamOffset.fromStart(testStreamKey))

        assertThat(messages).isNotNull
        assertThat(messages).hasSize(5)

        // 발행 순서가 유지되는지 검증
        messages!!.forEachIndexed { index, message ->
            val expectedEventId = "event-${index + 1}"
            assertThat(message.value["eventId"]).isEqualTo(expectedEventId)

            val payload = message.value["payload"]
            val parsedEvent = objectMapper.readValue(payload, TestDomainEvent::class.java)
            assertThat(parsedEvent.testData).isEqualTo("순서 테스트 ${index + 1}")
        }
    }

    @Test
    fun `이벤트 메타데이터가 올바르게 설정되는지 검증`() {
        // Given
        val now = LocalDateTime.now()
        val testEvent =
            TestDomainEvent(
                eventId = "metadata-test-id",
                eventType = "CUSTOM_TEST_EVENT",
                occurredAt = now,
                aggregateId = "metadata-user-123",
                testData = "메타데이터 테스트"
            )

        // When
        eventBus.publish(testEvent)

        // Then
        val streamOps = redisTemplate.opsForStream<String, String>()
        val messages = streamOps.read(StreamOffset.fromStart(testStreamKey))

        assertThat(messages).hasSize(1)
        val message = messages!!.first()

        // 메타데이터 검증
        assertThat(message.value["eventType"]).isEqualTo("CUSTOM_TEST_EVENT")
        assertThat(message.value["eventId"]).isEqualTo("metadata-test-id")
        assertThat(message.value["aggregateId"]).isEqualTo("metadata-user-123")
        assertThat(message.value["occurredAt"]).isNotBlank

        // 시간 검증 (발행 시간이 현재 시간과 비슷한지)
        val occurredAtStr = message.value["occurredAt"]
        val occurredAt = LocalDateTime.parse(occurredAtStr)
        assertThat(occurredAt).isBetween(now.minusMinutes(1), now.plusMinutes(1))
    }

    data class TestDomainEvent(
        override val eventId: String,
        override val eventType: String = "TEST_EVENT",
        override val occurredAt: LocalDateTime = LocalDateTime.now(),
        override val aggregateId: String,
        val testData: String
    ) : DomainEvent
}
