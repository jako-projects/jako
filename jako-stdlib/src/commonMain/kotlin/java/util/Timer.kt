@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.lang.Runnable

expect open class Timer {
    constructor()
    constructor(isDaemon: Boolean)
    constructor(name: String)
    constructor(name: String, isDaemon: Boolean)
    fun schedule(task: TimerTask, delay: Long)
    fun schedule(task: TimerTask, delay: Long, period: Long)
    fun cancel()
}

expect abstract class TimerTask : Runnable {
    constructor()
    abstract override fun run()
    open fun cancel(): Boolean
    open fun scheduledExecutionTime(): Long
}
