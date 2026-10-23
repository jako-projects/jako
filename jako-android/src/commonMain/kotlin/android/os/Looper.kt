@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

/**
 * Class used to run a message loop for a thread.
 */
expect final class Looper {
    val thread: Thread
    val queue: MessageQueue
    val isCurrentThread: Boolean

    fun getThread(): Thread
    fun getQueue(): MessageQueue
    fun isCurrentThread(): Boolean
    fun quit()
    fun quitSafely()
    override fun toString(): String

    companion object {
        fun prepare()
        fun prepareMainLooper()
        fun getMainLooper(): Looper?
        fun myLooper(): Looper?
        fun myQueue(): MessageQueue
        fun loop()
    }
}
