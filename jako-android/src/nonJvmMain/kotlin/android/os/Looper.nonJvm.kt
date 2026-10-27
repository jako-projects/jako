package android.os

import java.lang.ThreadLocal
import kotlin.concurrent.withLock

actual final class Looper private constructor(quitAllowed: Boolean) {
    actual val queue: MessageQueue = MessageQueue(quitAllowed)
    actual val thread: Thread = Thread.currentThread()

    actual val isCurrentThread: Boolean
        get() = isCurrentThread()

    actual fun getThread(): Thread = thread

    actual fun getQueue(): MessageQueue = queue

    actual fun isCurrentThread(): Boolean = Thread.currentThread() === thread

    actual fun quit() {
        queue.quit(false)
    }

    actual fun quitSafely() {
        queue.quit(true)
    }

    actual override fun toString(): String {
        return "Looper (${thread.name}, tid ${thread.id})"
    }

    actual companion object {
        private val threadLocal = ThreadLocal<Looper?>()
        private val mainLock = Any()
        private var mainLooper: Looper? = null

        actual fun prepare() {
            prepare(true)
        }

        private fun prepare(quitAllowed: Boolean) {
            if (threadLocal.get() != null) {
                throw RuntimeException("Only one Looper may be created per thread")
            }
            threadLocal.set(Looper(quitAllowed))
        }

        actual fun prepareMainLooper() {
            prepare(false)
            synchronized(mainLock) {
                if (mainLooper != null) {
                    throw IllegalStateException("The main Looper has already been prepared.")
                }
                mainLooper = myLooper()
            }
        }

        actual fun getMainLooper(): Looper? = synchronized(mainLock) { mainLooper }

        actual fun myLooper(): Looper? = threadLocal.get()

        actual fun myQueue(): MessageQueue {
            return myLooper()?.queue ?: throw NullPointerException("No Looper; Looper.prepare() wasn't called on this thread.")
        }

        actual fun loop() {
            val me = myLooper() ?: throw RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.")
            val queue = me.queue
            while (true) {
                val msg = queue.next() ?: return
                msg.target?.dispatchMessage(msg)
                msg.recycle()
            }
        }
    }
}
