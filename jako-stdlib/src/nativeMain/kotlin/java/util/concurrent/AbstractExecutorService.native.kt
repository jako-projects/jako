package java.util.concurrent

import java.lang.Runnable
import java.lang.System
import kotlin.concurrent.withLock

actual abstract class AbstractExecutorService actual constructor() : ExecutorService {
    actual override fun <T> submit(task: Callable<T>): Future<T> {
        val ftask = FutureTask(task)
        execute(ftask)
        return ftask
    }

    actual override fun <T> submit(task: Runnable, result: T): Future<T> {
        val ftask = FutureTask(task, result)
        execute(ftask)
        return ftask
    }

    actual override fun submit(task: Runnable): Future<*> {
        val ftask = FutureTask<Any?>(task, null)
        execute(ftask)
        return ftask
    }

    actual override fun <T> invokeAll(tasks: Collection<Callable<T>>): List<Future<T>> {
        val futures = ArrayList<Future<T>>(tasks.size)
        var done = false
        try {
            for (t in tasks) {
                val f = FutureTask(t)
                futures.add(f)
                execute(f)
            }
            for (f in futures) {
                if (!f.isDone()) {
                    try {
                        f.get()
                    } catch (_: CancellationException) {
                    } catch (_: ExecutionException) {
                    }
                }
            }
            done = true
            return futures
        } finally {
            if (!done) {
                for (f in futures) f.cancel(true)
            }
        }
    }

    actual override fun <T> invokeAll(
        tasks: Collection<Callable<T>>,
        timeout: Long,
        unit: TimeUnit
    ): List<Future<T>> {
        val nanos = unit.toNanos(timeout)
        val deadline = System.nanoTime() + nanos
        val futures = ArrayList<Future<T>>(tasks.size)
        var done = false
        try {
            for (t in tasks) {
                futures.add(FutureTask(t))
            }
            for (f in futures) {
                if (System.nanoTime() >= deadline) return futures
                execute(f as Runnable)
            }
            for (f in futures) {
                if (!f.isDone()) {
                    val rem = deadline - System.nanoTime()
                    if (rem <= 0L) return futures
                    try {
                        f.get(rem, TimeUnit.NANOSECONDS)
                    } catch (_: CancellationException) {
                    } catch (_: ExecutionException) {
                    } catch (_: TimeoutException) {
                        return futures
                    }
                }
            }
            done = true
            return futures
        } finally {
            if (!done) {
                for (f in futures) f.cancel(true)
            }
        }
    }

    actual override fun <T> invokeAny(tasks: Collection<Callable<T>>): T {
        if (tasks.isEmpty()) throw IllegalArgumentException()
        val ecs = ExecutorCompletionService<T>(this)
        val futures = ArrayList<Future<T>>(tasks.size)
        var lastEx: ExecutionException? = null
        try {
            for (t in tasks) {
                futures.add(ecs.submit(t))
            }
            var remaining = futures.size
            while (remaining > 0) {
                val f = ecs.take()
                remaining--
                try {
                    return f.get()
                } catch (e: ExecutionException) {
                    lastEx = e
                } catch (re: RuntimeException) {
                    lastEx = ExecutionException(re)
                }
            }
            throw lastEx ?: ExecutionException(null as Throwable?)
        } finally {
            for (f in futures) f.cancel(true)
        }
    }

    actual override fun <T> invokeAny(
        tasks: Collection<Callable<T>>,
        timeout: Long,
        unit: TimeUnit
    ): T {
        if (tasks.isEmpty()) throw IllegalArgumentException()
        val deadline = System.nanoTime() + unit.toNanos(timeout)
        val ecs = ExecutorCompletionService<T>(this)
        val futures = ArrayList<Future<T>>(tasks.size)
        var lastEx: ExecutionException? = null
        try {
            for (t in tasks) {
                futures.add(ecs.submit(t))
            }
            var remaining = futures.size
            while (remaining > 0) {
                val rem = deadline - System.nanoTime()
                if (rem <= 0L) throw TimeoutException()
                val f = ecs.poll(rem, TimeUnit.NANOSECONDS) ?: throw TimeoutException()
                remaining--
                try {
                    return f.get()
                } catch (e: ExecutionException) {
                    lastEx = e
                } catch (re: RuntimeException) {
                    lastEx = ExecutionException(re)
                }
            }
            throw lastEx ?: ExecutionException(null as Throwable?)
        } finally {
            for (f in futures) f.cancel(true)
        }
    }
}
