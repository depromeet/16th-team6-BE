package com.deepromeet.atcha.support

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class WatchdogResourceTest {
    private val taskCount = 1000
    private val repeatCount = 10
    private val delayMillis = 200L

    @Test
    fun `데몬 쓰레드 기반 감시 작업`() {
        val latch = CountDownLatch(taskCount)

        repeat(taskCount) { i ->
            thread(start = true, isDaemon = true, name = "watchdog-$i") {
                try {
                    repeat(repeatCount) {
                        Thread.sleep(delayMillis)
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        println("🧵 [데몬] 감시 쓰레드 ${taskCount}개 시작됨")
        Thread.sleep(1000) // 스레드들 생성된 후 측정
        println("🔍 [데몬] 활성 쓰레드 수: ${Thread.activeCount()}")

        latch.await()
        println("✅ [데몬] 감시 종료 완료")
    }

    @Test
    fun `코루틴 기반 감시 작업`() =
        runBlocking {
            val completed = AtomicInteger(0)
            val jobs = mutableListOf<Job>()

            repeat(taskCount) { i ->
                val job =
                    launch(Dispatchers.Default) {
                        repeat(repeatCount) {
                            delay(delayMillis)
                        }
                        completed.incrementAndGet()
                    }
                jobs.add(job)
            }

            println("🌿 [코루틴] 감시 ${taskCount}개 시작됨")
            delay(1000) // 코루틴들 실행된 후 측정
            println("🔍 [코루틴] 활성 쓰레드 수 (대략): ${Thread.activeCount()}")

            jobs.joinAll()
            println("✅ [코루틴] 감시 종료 완료 (총 완료: ${completed.get()})")
        }
}
