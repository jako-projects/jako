package java.util.concurrent

import java.lang.Runnable

actual fun interface Callable<V> {
    actual fun call(): V
}

actual interface Future<V> {
    actual fun cancel(mayInterruptIfRunning: Boolean): Boolean
    actual fun isCancelled(): Boolean
    actual fun isDone(): Boolean
    actual val isCancelled: Boolean
    actual val isDone: Boolean
    actual fun get(): V
    actual fun get(timeout: Long, unit: TimeUnit): V
}

actual interface RunnableFuture<V> : Runnable, Future<V> {
    actual override fun run()
}

actual open class ExecutionException : Exception {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}

actual open class TimeoutException : Exception {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}

actual open class CancellationException : IllegalStateException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}

actual open class FutureTask<V> : RunnableFuture<V> {
    private var callable: Callable<V>? = null
    private val latch = CountDownLatch(1)
    private var result: Any? = null
    private var exception: Throwable? = null

    @Volatile
    private var state: Int = NEW

    companion object {
        private const val NEW = 0
        private const val COMPLETING = 1
        private const val NORMAL = 2
        private const val EXCEPTIONAL = 3
        private const val CANCELLED = 4
    }

    actual constructor(callable: Callable<V>) {
        this.callable = callable
        this.state = NEW
    }

    actual constructor(runnable: Runnable, result: V) {
        this.callable = Callable {
            runnable.run()
            result
        }
        this.state = NEW
    }

    actual override fun run() {
        if (state != NEW) return
        state = COMPLETING
        val c = callable
        if (c != null) {
            try {
                val res = c.call()
                result = res
                state = NORMAL
            } catch (t: Throwable) {
                exception = t
                state = EXCEPTIONAL
            } finally {
                latch.countDown()
            }
        }
    }

    actual override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        if (state != NEW) return false
        state = CANCELLED
        latch.countDown()
        return true
    }

    actual override fun isCancelled(): Boolean = state == CANCELLED

    actual override fun isDone(): Boolean = state != NEW && state != COMPLETING

    actual override val isCancelled: Boolean get() = isCancelled()

    actual override val isDone: Boolean get() = isDone()

    @Suppress("UNCHECKED_CAST")
    actual override fun get(): V {
        latch.await()
        return report()
    }

    @Suppress("UNCHECKED_CAST")
    actual override fun get(timeout: Long, unit: TimeUnit): V {
        if (!latch.await(timeout, unit)) {
            throw TimeoutException("Timed out waiting for FutureTask")
        }
        return report()
    }

    @Suppress("UNCHECKED_CAST")
    private fun report(): V {
        return when (state) {
            NORMAL -> result as V
            CANCELLED -> throw CancellationException("Task was cancelled")
            EXCEPTIONAL -> throw ExecutionException(exception?.message, exception)
            else -> throw IllegalStateException("Task is not done")
        }
    }
}
