// test/kotlin/com/deepromeet/atcha/shared/event/infrastructure/RedisStreamEventConsumerIntegrationTest.kt
package com.deepromeet.atcha.shared.event

import com.deepromeet.atcha.shared.domain.event.domain.DomainEvent
import com.deepromeet.atcha.shared.domain.event.domain.EventBus
import com.deepromeet.atcha.shared.domain.event.domain.EventHandler
import com.deepromeet.atcha.shared.infrastructure.event.redis.RedisStreamEventConsumer
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.TestPropertySource
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@SpringBootTest
@TestPropertySource(
    properties = [
        "redis.stream.event.key=integration-test:events",
        "redis.stream.event.group=integration-test-group",
        "redis.stream.event.dead-letter.key=integration-test:events:dead-letter"
    ]
)
class RedisStreamEventConsumerIntegrationTest {
    @Autowired
    private lateinit var eventBus: EventBus

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    private lateinit var testEventHandler: TestEventHandler

    @Autowired
    private lateinit var errorEventHandler: ErrorEventHandler

    @Autowired
    private lateinit var eventConsumer: RedisStreamEventConsumer

    private val testStreamKey = "integration-test:events"
    private val deadLetterKey = "integration-test:events:dead-letter"
    private val groupName = "integration-test-group"

    @TestConfiguration
    class TestEventHandlerConfig {
        @Bean
        fun testEventHandler(): TestEventHandler = TestEventHandler()

        @Bean
        fun errorEventHandler(): ErrorEventHandler = ErrorEventHandler()
    }

    @BeforeEach
    fun setUp() {
        // 테스트 전 스트림 정리
        redisTemplate.delete(testStreamKey)
        redisTemplate.delete(deadLetterKey)

        // Stream과 Consumer Group 초기화
        initializeStreamAndGroup()

        // 핸들러 상태 초기화
        testEventHandler.reset()
        errorEventHandler.reset()
    }

    private fun initializeStreamAndGroup() {
        val streamOps = redisTemplate.opsForStream<String, String>()

        try {
            // 1. 빈 메시지로 Stream 생성 (Stream이 없으면 Consumer Group 생성 불가)
            if (!redisTemplate.hasKey(testStreamKey)) {
                streamOps.add(testStreamKey, mapOf("init" to "init"))
            }

            // 2. Consumer Group 생성
            streamOps.createGroup(testStreamKey, groupName)

            // 3. 초기화 메시지 정리 (있다면)
            val initMessages = streamOps.read(StreamOffset.fromStart(testStreamKey))
            initMessages?.forEach { message ->
                if (message.value.containsKey("init")) {
                    streamOps.delete(testStreamKey, message.id)
                }
            }
        } catch (e: Exception) {
            println("Stream 초기화 중 오류 발생: ${e.message}")
            // 테스트 계속 진행
        }
    }

    @AfterEach
    fun tearDown() {
        try {
            val streamOps = redisTemplate.opsForStream<String, String>()

            // Consumer Group 삭제 (있다면)
            try {
                streamOps.destroyGroup(testStreamKey, groupName)
            } catch (e: Exception) {
                // Group이 없으면 무시
            }

            // 테스트 후 스트림 정리
            redisTemplate.delete(testStreamKey)
            redisTemplate.delete(deadLetterKey)
        } catch (e: Exception) {
            println("Stream 정리 중 오류 발생: ${e.message}")
        }
    }

    @Test
    fun `이벤트 발행 후 Consumer가 정상 처리하는지 검증`() {
        // Given
        val testEvent =
            TestDomainEvent(
                eventId = UUID.randomUUID().toString(),
                aggregateId = "user-123",
                testData = "통합 테스트 데이터"
            )

        // When
        eventBus.publish(testEvent)

        // 스케줄러 대기 대신 직접 호출
        eventConsumer.consumeEvents()

        // Then - 즉시 검증
        assertThat(testEventHandler.processedEvents).hasSize(1)
        assertThat(testEventHandler.processedEvents[0].eventId).isEqualTo(testEvent.eventId)
        assertThat(testEventHandler.processedEvents[0].testData).isEqualTo(testEvent.testData)

        // Redis에서 메시지가 acknowledge 되었는지 확인
        val streamOps = redisTemplate.opsForStream<String, String>()
        val pendingMessages = streamOps.pending(testStreamKey, groupName)
        assertThat(pendingMessages?.totalPendingMessages).isEqualTo(0L)
    }

    @Test
    fun `여러 이벤트 배치 발행 후 모두 처리되는지 검증`() {
        // Given
        val events =
            (1..5).map { index ->
                TestDomainEvent(
                    eventId = UUID.randomUUID().toString(),
                    aggregateId = "user-$index",
                    testData = "배치 테스트 데이터 $index"
                )
            }

        // When
        eventBus.publishAll(events)

        // 직접 호출로 즉시 처리
        eventConsumer.consumeEvents()

        // Then - 즉시 검증
        assertThat(testEventHandler.processedEvents).hasSize(5)

        val processedEventIds = testEventHandler.processedEvents.map { it.eventId }
        val originalEventIds = events.map { it.eventId }
        assertThat(processedEventIds).containsExactlyInAnyOrderElementsOf(originalEventIds)

        // 모든 메시지가 acknowledge 되었는지 확인
        val streamOps = redisTemplate.opsForStream<String, String>()
        val pendingMessages = streamOps.pending(testStreamKey, groupName)
        assertThat(pendingMessages?.totalPendingMessages).isEqualTo(0L)
    }

    @Test
    fun `지원하지 않는 이벤트 타입은 Dead Letter로 이동하는지 검증`() {
        // Given
        val unsupportedEvent =
            UnsupportedDomainEvent(
                eventId = UUID.randomUUID().toString(),
                aggregateId = "user-999"
            )

        // When
        eventBus.publish(unsupportedEvent)

        // 직접 호출로 즉시 처리
        eventConsumer.consumeEvents()

        // Then - 즉시 검증
        val streamOps = redisTemplate.opsForStream<String, String>()
        val deadLetterMessages = streamOps.read(StreamOffset.fromStart(deadLetterKey))

        assertThat(deadLetterMessages).isNotNull
        assertThat(deadLetterMessages).hasSize(1)

        val message = deadLetterMessages!!.first()
        assertThat(message.value["eventType"]).isEqualTo("UNSUPPORTED_EVENT")
        assertThat(message.value["eventId"]).isEqualTo(unsupportedEvent.eventId)

        // 핸들러는 호출되지 않아야 함
        assertThat(testEventHandler.processedEvents).isEmpty()
    }

    @Test
    fun `이벤트 처리 중 예외 발생 시 Dead Letter로 이동하는지 검증`() {
        // Given
        val errorEvent =
            ErrorDomainEvent(
                eventId = UUID.randomUUID().toString(),
                aggregateId = "error-user"
            )

        // When
        eventBus.publish(errorEvent)

        // 직접 호출로 즉시 처리
        eventConsumer.consumeEvents()

        // Then - 즉시 검증
        assertThat(errorEventHandler.processCount.get()).isEqualTo(1)
        assertThat(errorEventHandler.lastError.get()).isNotNull

        // Dead Letter에 메시지가 들어갔는지 확인
        val streamOps = redisTemplate.opsForStream<String, String>()
        val deadLetterMessages = streamOps.read(StreamOffset.fromStart(deadLetterKey))

        assertThat(deadLetterMessages).isNotNull
        assertThat(deadLetterMessages).hasSize(1)

        val message = deadLetterMessages!!.first()
        assertThat(message.value["eventType"]).isEqualTo("ERROR_EVENT")
        assertThat(message.value["eventId"]).isEqualTo(errorEvent.eventId)
    }

    @Test
    fun `Consumer가 여러 번 실행되어도 중복 처리되지 않는지 검증`() {
        // Given
        val testEvent =
            TestDomainEvent(
                eventId = UUID.randomUUID().toString(),
                aggregateId = "duplicate-test-user",
                testData = "중복 처리 방지 테스트"
            )

        // When
        eventBus.publish(testEvent)

        // 첫 번째 처리
        eventConsumer.consumeEvents()
        val firstProcessCount = testEventHandler.processedEvents.size

        // 두 번째 처리 (중복 실행)
        eventConsumer.consumeEvents()

        // Then - 추가 처리가 일어나지 않아야 함
        assertThat(testEventHandler.processedEvents).hasSize(firstProcessCount)
        assertThat(testEventHandler.processedEvents).hasSize(1) // 한 번만 처리됨
    }

    @Test
    fun `Stream과 Consumer Group이 올바르게 생성되는지 검증`() {
        // Given & When - setUp에서 이미 초기화됨

        // Then
        val streamOps = redisTemplate.opsForStream<String, String>()

        // Stream 존재 확인
        assertThat(redisTemplate.hasKey(testStreamKey)).isTrue

        // Consumer Group 확인
        val groups = streamOps.groups(testStreamKey)
        assertThat(groups).hasSize(1)
        assertThat(groups[0].groupName()).isEqualTo(groupName)

        println("✅ Stream과 Consumer Group 생성 완료")
        println("Stream Key: $testStreamKey")
        println("Group Name: $groupName")
    }

    // 테스트용 도메인 이벤트들
    data class TestDomainEvent(
        override val eventId: String,
        override val eventType: String = "TEST_EVENT",
        override val occurredAt: LocalDateTime = LocalDateTime.now(),
        override val aggregateId: String,
        val testData: String
    ) : DomainEvent

    data class UnsupportedDomainEvent(
        override val eventId: String,
        override val eventType: String = "UNSUPPORTED_EVENT",
        override val occurredAt: LocalDateTime = LocalDateTime.now(),
        override val aggregateId: String
    ) : DomainEvent

    data class ErrorDomainEvent(
        override val eventId: String,
        override val eventType: String = "ERROR_EVENT",
        override val occurredAt: LocalDateTime = LocalDateTime.now(),
        override val aggregateId: String
    ) : DomainEvent
}

class TestEventHandler : EventHandler {
    val processedEvents = mutableListOf<RedisStreamEventConsumerIntegrationTest.TestDomainEvent>()

    override fun supports(eventType: String): Boolean = eventType == "TEST_EVENT"

    override fun handle(eventPayload: String) {
        val objectMapper =
            ObjectMapper().apply {
                findAndRegisterModules()
            }
        val event =
            objectMapper.readValue(
                eventPayload,
                RedisStreamEventConsumerIntegrationTest.TestDomainEvent::class.java
            )
        synchronized(processedEvents) {
            processedEvents.add(event)
        }
    }

    fun reset() {
        synchronized(processedEvents) {
            processedEvents.clear()
        }
    }
}

class ErrorEventHandler : EventHandler {
    val processCount = AtomicInteger(0)
    val lastError = AtomicReference<Exception>()

    override fun supports(eventType: String): Boolean = eventType == "ERROR_EVENT"

    override fun handle(eventPayload: String) {
        processCount.incrementAndGet()
        val error = RuntimeException("테스트용 에러 발생")
        lastError.set(error)
        throw error
    }

    fun reset() {
        processCount.set(0)
        lastError.set(null)
    }
}
