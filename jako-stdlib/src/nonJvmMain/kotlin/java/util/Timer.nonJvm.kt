package java.util

import com.bennyhuo.jako.WebTaskScheduler
import java.lang.Runnable
import java.lang.System
import java.lang.Thread
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

actual open class Timer {
    private val lock = ReentrantLock()
    private val wake = lock.newCondition()
    private val queue = ArrayList<TimerQueueEntry>()
    private var cancelled = false
    private val thread: Thread
    private var unregisterHook: (() -> Unit)? = null

    actual constructor() : this(nextTimerName(), false)
    actual constructor(isDaemon: Boolean) : this(nextTimerName(), isDaemon)
    actual constructor(name: String) : this(name, false)
    actual constructor(name: String, isDaemon: Boolean) {
        val self = this
        thread = Thread(Runnable { self.mainLoop() }, name)
        thread.setDaemon(isDaemon)
        if (WebTaskScheduler.isMultiThreadingSupported()) {
            thread.start()
        } else {
            unregisterHook = WebTaskScheduler.registerPollingHook { self.pollSingleStep() }
        }
    }

    actual fun schedule(task: TimerTask, delay: Long) {
        sched(task, delay, 0L)
    }

    actual fun schedule(task: TimerTask, delay: Long, period: Long) {
        if (period <= 0L) throw IllegalArgumentException("Non-positive period.")
        sched(task, delay, period)
    }

    actual fun cancel() {
        lock.lock()
        try {
            cancelled = true
            queue.clear()
            unregisterHook?.invoke()
            unregisterHook = null
            wake.signalAll()
        } finally {
            lock.unlock()
        }
    }

    private fun sched(task: TimerTask, delay: Long, period: Long) {
        if (delay < 0L) throw IllegalArgumentException("Negative delay.")
        lock.lock()
        try {
            if (cancelled) throw IllegalStateException("Timer already cancelled.")
            if (!task.markScheduled()) {
                throw IllegalStateException("Task already scheduled or cancelled")
            }
            queue.add(TimerQueueEntry(task, System.currentTimeMillis() + delay, period))
            wake.signalAll()
        } finally {
            lock.unlock()
        }
    }

    private fun pollSingleStep(): Boolean {
        var toRun: TimerTask? = null
        lock.lock()
        try {
            if (cancelled || queue.isEmpty()) return false
            var earliestIndex = 0
            var earliest = queue[0]
            for (i in 1 until queue.size) {
                if (queue[i].nextTime < earliest.nextTime) {
                    earliest = queue[i]
                    earliestIndex = i
                }
            }
            val waitMs = earliest.nextTime - System.currentTimeMillis()
            if (waitMs > 0L) return false
            queue.removeAt(earliestIndex)
            if (earliest.task.isCancelled()) return true
            earliest.task.recordExecution(earliest.nextTime)
            if (earliest.period > 0L && !earliest.task.isCancelled()) {
                earliest.nextTime = System.currentTimeMillis() + earliest.period
                queue.add(earliest)
            }
            toRun = earliest.task
        } finally {
            lock.unlock()
        }
        try {
            toRun?.run()
        } catch (_: Throwable) {
            cancel()
        }
        return true
    }

    private fun mainLoop() {
        while (true) {
            var toRun: TimerTask? = null
            lock.lock()
            try {
                while (!cancelled && queue.isEmpty()) {
                    wake.await()
                }
                if (cancelled) return
                var earliestIndex = 0
                var earliest = queue[0]
                for (i in 1 until queue.size) {
                    if (queue[i].nextTime < earliest.nextTime) {
                        earliest = queue[i]
                        earliestIndex = i
                    }
                }
                val waitMs = earliest.nextTime - System.currentTimeMillis()
                if (waitMs > 0L) {
                    wake.awaitNanos(waitMs * 1_000_000L)
                    continue
                }
                queue.removeAt(earliestIndex)
                if (earliest.task.isCancelled()) continue
                earliest.task.recordExecution(earliest.nextTime)
                if (earliest.period > 0L && !earliest.task.isCancelled()) {
                    earliest.nextTime = System.currentTimeMillis() + earliest.period
                    queue.add(earliest)
                }
                toRun = earliest.task
            } finally {
                lock.unlock()
            }
            try {
                toRun?.run()
            } catch (t: Throwable) {
                cancel()
                return
            }
        }
    }

    companion object {
        private val seq = AtomicInteger(0)
        private fun nextTimerName(): String = "Timer-${seq.getAndIncrement()}"
    }
}

internal class TimerQueueEntry(
    val task: TimerTask,
    var nextTime: Long,
    val period: Long
)
