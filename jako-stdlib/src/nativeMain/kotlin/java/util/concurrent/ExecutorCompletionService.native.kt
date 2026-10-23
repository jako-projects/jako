package java.util.concurrent

import java.lang.Runnable
import kotlin.concurrent.withLock

actual open class ExecutorCompletionService<V> : CompletionService<V> {
    private val executor: Executor
    private val completionQueue: BlockingQueue<Future<V>>

    actual constructor(executor: Executor) : this(executor, LinkedBlockingQueue())

    actual constructor(executor: Executor, completionQueue: BlockingQueue<Future<V>>) {
        this.executor = executor
        this.completionQueue = completionQueue
    }

    private fun wrapAndExecute(ftask: FutureTask<V>): Future<V> {
        executor.execute(Runnable {
            try {
                ftask.run()
            } finally {
                completionQueue.add(ftask)
            }
        })
        return ftask
    }

    actual override fun submit(task: Callable<V>): Future<V> {
        return wrapAndExecute(FutureTask(task))
    }

    actual override fun submit(task: Runnable, result: V): Future<V> {
        return wrapAndExecute(FutureTask(task, result))
    }

    actual override fun take(): Future<V> = completionQueue.take()

    actual override fun poll(): Future<V>? = completionQueue.poll()

    actual override fun poll(timeout: Long, unit: TimeUnit): Future<V>? =
        completionQueue.poll(timeout, unit)
}
