package java.util.concurrent

import com.bennyhuo.jako.WebTaskScheduler
import java.lang.Runnable
import kotlin.concurrent.withLock

actual open class ThreadPoolExecutor actual constructor(
    corePoolSize: Int,
    maximumPoolSize: Int,
    keepAliveTime: Long,
    unit: TimeUnit,
    workQueue: BlockingQueue<Runnable>,
    private val threadFactory: ThreadFactory
) : AbstractExecutorService() {
    private var isShutdownState = false

    init {
        if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize || keepAliveTime < 0L) {
            throw IllegalArgumentException()
        }
    }

    actual override fun execute(command: Runnable) {
        if (isShutdownState) throw RejectedExecutionException("Executor is shutdown")
        val worker = threadFactory.newThread(command)
        if (WebTaskScheduler.isMultiThreadingSupported()) {
            worker.start()
        } else {
            WebTaskScheduler.schedule(0L) {
                worker.run()
            }
        }
    }

    actual override fun <T> submit(task: Callable<T>): Future<T> = super.submit(task)

    actual override fun <T> submit(task: Runnable, result: T): Future<T> = super.submit(task, result)

    actual override fun submit(task: Runnable): Future<*> = super.submit(task)

    actual override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        return isShutdownState
    }

    actual override fun shutdown() {
        isShutdownState = true
    }

    actual override fun shutdownNow(): List<Runnable> {
        isShutdownState = true
        return emptyList()
    }

    actual override fun isShutdown(): Boolean = isShutdownState
    actual override fun isTerminated(): Boolean = isShutdownState
}
