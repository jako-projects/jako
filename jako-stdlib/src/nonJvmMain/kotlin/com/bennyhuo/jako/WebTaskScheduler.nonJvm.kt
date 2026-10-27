package com.bennyhuo.jako

import java.lang.System

internal expect fun isMultiThreadingSupported(): Boolean

internal expect fun platformSleepSliceMillis(ms: Int)

/**
 * Cooperative single-threaded task & event loop scheduler for non-multi-threaded targets (`jsMain`, `wasmJsMain`).
 *
 * Drives scheduled tasks (`Timer`, `ThreadPoolExecutor`, `ScheduledThreadPoolExecutor`, `ForkJoinPool`)
 * and polling hooks (`android.os.HandlerThread` `Looper`) whenever the main thread waits on a
 * `Condition`, `Object.wait`, `Future.get`, or `Thread.sleep`.
 */
object WebTaskScheduler {
    private class ScheduledEntry(
        val id: Long,
        var triggerNanos: Long,
        val action: () -> Unit,
        var cancelled: Boolean = false
    )

    private var nextId = 1L
    private val tasks = ArrayList<ScheduledEntry>()
    private val pollingHooks = LinkedHashMap<Long, () -> Boolean>()
    private var pumpingDepth = 0

    fun isMultiThreadingSupported(): Boolean = com.bennyhuo.jako.isMultiThreadingSupported()

    fun schedule(delayNanos: Long, action: () -> Unit): () -> Unit {
        val id = nextId++
        val trigger = System.nanoTime() + maxOf(0L, delayNanos)
        val entry = ScheduledEntry(id, trigger, action)
        tasks.add(entry)
        return {
            entry.cancelled = true
            tasks.remove(entry)
        }
    }

    fun registerPollingHook(hook: () -> Boolean): () -> Unit {
        val id = nextId++
        pollingHooks[id] = hook
        return {
            pollingHooks.remove(id)
        }
    }

    fun hasPendingWork(): Boolean {
        return tasks.any { !it.cancelled } || pollingHooks.isNotEmpty()
    }

    fun pumpReadyTasks(): Boolean {
        var didWork = false
        pumpingDepth++
        try {
            var iterations = 0
            while (iterations < 8) {
                iterations++
                var stepWork = false
                val now = System.nanoTime()

                var earliestDueIdx = -1
                var earliestDueTime = Long.MAX_VALUE
                for (i in tasks.indices) {
                    val t = tasks[i]
                    if (!t.cancelled && t.triggerNanos <= now && t.triggerNanos < earliestDueTime) {
                        earliestDueTime = t.triggerNanos
                        earliestDueIdx = i
                    }
                }

                if (earliestDueIdx >= 0) {
                    val entry = tasks.removeAt(earliestDueIdx)
                    if (!entry.cancelled) {
                        stepWork = true
                        didWork = true
                        entry.action()
                    }
                }

                if (pollingHooks.isNotEmpty()) {
                    val hooksSnapshot = pollingHooks.values.toList()
                    for (hook in hooksSnapshot) {
                        if (hook()) {
                            stepWork = true
                            didWork = true
                        }
                    }
                }

                if (!stepWork) break
            }
        } finally {
            pumpingDepth--
        }
        return didWork
    }

    fun runNextTask(): Boolean = pumpReadyTasks()

    fun runAllAvailable(maxSteps: Int = 16): Boolean = pumpReadyTasks()


    fun sleepOrWait(nanosTimeout: Long, stopCondition: () -> Boolean = { false }): Long {
        if (nanosTimeout <= 0L) {
            pumpReadyTasks()
            return 0L
        }
        val start = System.nanoTime()
        val deadline = start + nanosTimeout
        while (true) {
            pumpReadyTasks()
            if (stopCondition()) {
                val elapsed = System.nanoTime() - start
                return (nanosTimeout - elapsed).coerceAtLeast(1L)
            }
            val now = System.nanoTime()
            val remainingNanos = deadline - now
            if (remainingNanos <= 0L) {
                return 0L
            }

            var nextWakeNanos = remainingNanos
            for (i in tasks.indices) {
                val t = tasks[i]
                if (!t.cancelled) {
                    val d = (t.triggerNanos - now).coerceAtLeast(0L)
                    if (d < nextWakeNanos) nextWakeNanos = d
                }
            }
            if (pollingHooks.isNotEmpty()) {
                nextWakeNanos = minOf(nextWakeNanos, 2_000_000L)
            }

            val sleepMs = minOf((nextWakeNanos / 1_000_000L).toInt().coerceAtLeast(1), 5)
            platformSleepSliceMillis(sleepMs)
        }
    }
}
