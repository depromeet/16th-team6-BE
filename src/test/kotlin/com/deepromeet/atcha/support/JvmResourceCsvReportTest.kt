import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Collections
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class JvmResourceCsvReportTest {
    private val taskCount = 100
    private val repeatCount = 10
    private val delayMillis = 100L
    private val iterationCount = 100

    private val csvFile = File("thread_count_result2.csv")

    init {
        // 헤더 작성 (type은 영어로: daemon / coroutine)
        if (!csvFile.exists()) {
            csvFile.writeText("type,exec,createdThreads\n")
        }
    }

    @Test
    fun `measure daemon thread count`() {
        repeat(iterationCount) { exec ->
            val createdThreads = Collections.synchronizedSet(mutableSetOf<String>())
            val latch = CountDownLatch(taskCount)

            repeat(taskCount) {
                thread(start = true, isDaemon = true) {
                    createdThreads.add(Thread.currentThread().id.toString())
                    repeat(repeatCount) {
                        Thread.sleep(delayMillis)
                    }
                    latch.countDown()
                }
            }

            latch.await()

            val row = "daemon,$exec,${createdThreads.size}"
            println(row)
        }
    }

    @Test
    fun `measure coroutine thread count`() =
        runBlocking {
            repeat(iterationCount) { exec ->
                val createdThreads = Collections.synchronizedSet(mutableSetOf<String>())
                val jobs = mutableListOf<Job>()

                repeat(taskCount) {
                    val job =
                        launch(Dispatchers.Default) {
                            val thread = Thread.currentThread()

                            createdThreads.add(thread.id.toString())

                            repeat(repeatCount) {
                                delay(delayMillis)
                            }
                        }
                    jobs.add(job)
                }

                jobs.joinAll()

                val row = "coroutine,$exec,${createdThreads.size}"
                csvFile.appendText(row + "\n")
            }
        }
}
