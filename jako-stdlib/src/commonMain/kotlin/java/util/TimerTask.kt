@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.lang.Runnable

expect abstract class TimerTask : Runnable {
    constructor()
    abstract override fun run()
    open fun cancel(): Boolean
    open fun scheduledExecutionTime(): Long
}
