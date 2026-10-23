package java.util.concurrent

import java.lang.Runnable
import java.lang.Thread
import kotlin.concurrent.withLock

actual object Executors {
    actual fun newSingleThreadExecutor(): ExecutorService = newSingleThreadExecutor(ThreadFactory { r -> Thread(r) })

    actual fun newSingleThreadExecutor(threadFactory: ThreadFactory): ExecutorService = ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue(),
        threadFactory
    )

    actual fun newFixedThreadPool(nThreads: Int): ExecutorService = newFixedThreadPool(nThreads, ThreadFactory { r -> Thread(r) })

    actual fun newFixedThreadPool(nThreads: Int, threadFactory: ThreadFactory): ExecutorService = ThreadPoolExecutor(
        nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue(),
        threadFactory
    )

    actual fun newCachedThreadPool(): ExecutorService = newCachedThreadPool(ThreadFactory { r -> Thread(r) })

    actual fun newCachedThreadPool(threadFactory: ThreadFactory): ExecutorService = ThreadPoolExecutor(
        0, Int.MAX_VALUE, 60L, TimeUnit.SECONDS,
        SynchronousQueue(),
        threadFactory
    )

    actual fun newSingleThreadScheduledExecutor(): ScheduledExecutorService =
        ScheduledThreadPoolExecutor(1)

    actual fun newSingleThreadScheduledExecutor(threadFactory: ThreadFactory): ScheduledExecutorService =
        ScheduledThreadPoolExecutor(1, threadFactory)

    actual fun newScheduledThreadPool(corePoolSize: Int): ScheduledExecutorService =
        ScheduledThreadPoolExecutor(corePoolSize)

    actual fun newScheduledThreadPool(corePoolSize: Int, threadFactory: ThreadFactory): ScheduledExecutorService =
        ScheduledThreadPoolExecutor(corePoolSize, threadFactory)

    actual fun newWorkStealingPool(parallelism: Int): ExecutorService =
        ForkJoinPool(parallelism)

    actual fun newWorkStealingPool(): ExecutorService =
        ForkJoinPool()

    actual fun unconfigurableExecutorService(executor: ExecutorService): ExecutorService =
        object : ExecutorService by executor {}

    actual fun unconfigurableScheduledExecutorService(executor: ScheduledExecutorService): ScheduledExecutorService =
        object : ScheduledExecutorService by executor {}

    actual fun defaultThreadFactory(): ThreadFactory = ThreadFactory { r -> Thread(r) }

    actual fun <T> callable(task: Runnable, result: T): Callable<T> = Callable {
        task.run()
        result
    }

    actual fun callable(task: Runnable): Callable<Any?> = Callable {
        task.run()
        null
    }
}
