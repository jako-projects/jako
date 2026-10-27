package java.util.concurrent

import com.bennyhuo.jako.WebTaskScheduler
import java.lang.InterruptedException
import java.lang.Runnable
import java.lang.System
import java.lang.Thread
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.withLock

internal class ScheduledFutureTask<V>(
    callable: Callable<V>,
    private val triggerTimeNanos: Long
) : FutureTask<V>(callable), ScheduledFuture<V> {
    override fun getDelay(unit: TimeUnit): Long {
        val remainingNanos = triggerTimeNanos - System.nanoTime()
        return unit.convert(remainingNanos, TimeUnit.NANOSECONDS)
    }

    override fun compareTo(other: Delayed): Int {
        if (other === this) return 0
        val diff = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS)
        return when {
            diff < 0L -> -1
            diff > 0L -> 1
            else -> 0
        }
    }
}

actual open class ScheduledThreadPoolExecutor actual constructor(
    corePoolSize: Int,
    private val threadFactory: ThreadFactory
) : ThreadPoolExecutor(
    corePoolSize,
    Int.MAX_VALUE,
    0L,
    TimeUnit.NANOSECONDS,
    LinkedBlockingQueue(),
    threadFactory
), ScheduledExecutorService {

    actual constructor(corePoolSize: Int) : this(corePoolSize, ThreadFactory { r -> Thread(r) })

    init {
        if (corePoolSize < 0) throw IllegalArgumentException()
    }

    actual override fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*> {
        return schedule(Callable<Any?> { command.run(); null }, delay, unit)
    }

    actual override fun <V> schedule(callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledFuture<V> {
        if (isShutdown()) throw RejectedExecutionException("Executor is shutdown")
        val delayNanos = maxOf(0L, unit.toNanos(delay))
        val triggerNanos = System.nanoTime() + delayNanos
        val task = ScheduledFutureTask(callable, triggerNanos)
        if (WebTaskScheduler.isMultiThreadingSupported()) {
            threadFactory.newThread(Runnable {
                val sleepMillis = TimeUnit.NANOSECONDS.toMillis(delayNanos)
                val sleepNanos = (delayNanos % 1_000_000L).toInt()
                if (sleepMillis > 0 || sleepNanos > 0) {
                    try {
                        Thread.sleep(sleepMillis, sleepNanos)
                    } catch (_: InterruptedException) {
                        return@Runnable
                    }
                }
                if (!task.isCancelled() && !isShutdown()) {
                    task.run()
                }
            }).start()
        } else {
            WebTaskScheduler.schedule(delayNanos) {
                if (!task.isCancelled() && !isShutdown()) {
                    task.run()
                }
            }
        }
        return task
    }

    actual override fun scheduleAtFixedRate(
        command: Runnable,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit
    ): ScheduledFuture<*> {
        if (period <= 0L) throw IllegalArgumentException("period must be positive")
        return schedulePeriodic(command, initialDelay, period, unit, fixedRate = true)
    }

    actual override fun scheduleWithFixedDelay(
        command: Runnable,
        initialDelay: Long,
        delay: Long,
        unit: TimeUnit
    ): ScheduledFuture<*> {
        if (delay <= 0L) throw IllegalArgumentException("delay must be positive")
        return schedulePeriodic(command, initialDelay, delay, unit, fixedRate = false)
    }

    private fun schedulePeriodic(
        command: Runnable,
        initialDelay: Long,
        interval: Long,
        unit: TimeUnit,
        fixedRate: Boolean
    ): ScheduledFuture<*> {
        if (isShutdown()) throw RejectedExecutionException("Executor is shutdown")
        val initNanos = maxOf(0L, unit.toNanos(initialDelay))
        val intervalNanos = unit.toNanos(interval)
        val cancelled = AtomicBoolean(false)
        val done = AtomicBoolean(false)
        var workerThread: Thread? = null
        var webCancelHandle: (() -> Unit)? = null
        var nextTriggerNanos = System.nanoTime() + initNanos

        val future = object : ScheduledFuture<Any?> {
            override fun getDelay(unit: TimeUnit): Long {
                return unit.convert(nextTriggerNanos - System.nanoTime(), TimeUnit.NANOSECONDS)
            }

            override fun compareTo(other: Delayed): Int {
                if (other === this) return 0
                val d = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS)
                return if (d < 0) -1 else if (d > 0) 1 else 0
            }

            override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                if (done.get()) return false
                val changed = cancelled.compareAndSet(false, true)
                done.set(true)
                webCancelHandle?.invoke()
                if (changed && mayInterruptIfRunning) {
                    workerThread?.interrupt()
                }
                return changed
            }

            override fun isCancelled(): Boolean = cancelled.get()
            override val isCancelled: Boolean get() = cancelled.get()
            override fun isDone(): Boolean = done.get()
            override val isDone: Boolean get() = done.get()

            override fun get(): Any? {
                while (!done.get() && !isShutdown()) {
                    Thread.sleep(5)
                }
                if (cancelled.get()) throw CancellationException()
                return null
            }

            override fun get(timeout: Long, unit: TimeUnit): Any? {
                val deadline = System.nanoTime() + unit.toNanos(timeout)
                while (!done.get() && !isShutdown()) {
                    if (System.nanoTime() >= deadline) throw TimeoutException()
                    Thread.sleep(5)
                }
                if (cancelled.get()) throw CancellationException()
                return null
            }
        }

        if (WebTaskScheduler.isMultiThreadingSupported()) {
            val t = threadFactory.newThread(Runnable {
                try {
                    if (initNanos > 0) {
                        Thread.sleep(TimeUnit.NANOSECONDS.toMillis(initNanos), (initNanos % 1_000_000L).toInt())
                    }
                    while (!cancelled.get() && !isShutdown()) {
                        command.run()
                        if (cancelled.get() || isShutdown()) break
                        val sleepNanos = if (fixedRate) {
                            nextTriggerNanos += intervalNanos
                            maxOf(0L, nextTriggerNanos - System.nanoTime())
                        } else {
                            nextTriggerNanos = System.nanoTime() + intervalNanos
                            intervalNanos
                        }
                        if (sleepNanos > 0) {
                            Thread.sleep(TimeUnit.NANOSECONDS.toMillis(sleepNanos), (sleepNanos % 1_000_000L).toInt())
                        }
                    }
                } catch (_: InterruptedException) {
                    // Exit on interrupt
                } catch (_: Throwable) {
                    done.set(true)
                } finally {
                    done.set(true)
                }
            })
            workerThread = t
            t.start()
        } else {
            fun scheduleStep(delayNanos: Long) {
                webCancelHandle = WebTaskScheduler.schedule(delayNanos) {
                    if (cancelled.get() || isShutdown()) {
                        done.set(true)
                        return@schedule
                    }
                    try {
                        command.run()
                    } catch (_: Throwable) {
                        done.set(true)
                        return@schedule
                    }
                    if (cancelled.get() || isShutdown()) {
                        done.set(true)
                        return@schedule
                    }
                    val nextDelay = if (fixedRate) {
                        nextTriggerNanos += intervalNanos
                        maxOf(0L, nextTriggerNanos - System.nanoTime())
                    } else {
                        nextTriggerNanos = System.nanoTime() + intervalNanos
                        intervalNanos
                    }
                    scheduleStep(nextDelay)
                }
            }
            scheduleStep(initNanos)
        }
        return future
    }
}
