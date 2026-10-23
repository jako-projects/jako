@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

/**
 * Handy class for starting a new thread that has a looper.
 */
expect open class HandlerThread : Thread {
    constructor(name: String)
    constructor(name: String, priority: Int)

    val looper: Looper?
    val threadId: Int

    protected open fun onLooperPrepared()
    override fun run()
    open fun getLooper(): Looper?
    open fun quit(): Boolean
    open fun quitSafely(): Boolean
    open fun getThreadId(): Int
}
