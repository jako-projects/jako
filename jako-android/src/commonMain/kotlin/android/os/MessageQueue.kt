@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

/**
 * Low-level class holding the list of messages to be dispatched by a [Looper].
 */
expect final class MessageQueue {
    val isIdle: Boolean

    fun isIdle(): Boolean
    fun addIdleHandler(handler: IdleHandler)
    fun removeIdleHandler(handler: IdleHandler)

    fun interface IdleHandler {
        fun queueIdle(): Boolean
    }
}
