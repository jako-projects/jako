package android.os

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Looper private constructor(quitAllowed: Boolean) {
    private val mQueue = MessageQueue(quitAllowed)
    private val mThread: Thread = Thread.currentThread()

    val thread: Thread
        get() = mThread

    val queue: MessageQueue
        get() = mQueue

    val isCurrentThread: Boolean
        get() = Thread.currentThread() === mThread

    fun quit() {
        mQueue.quit(false)
    }

    fun quitSafely() {
        mQueue.quit(true)
    }

    override fun toString(): String = "Looper (${mThread.name}, tid ${mThread.id})"

    companion object {
        private val sThreadLocal = ThreadLocal<Looper?>()
        private val sMainLock = ReentrantLock()
        private var sMainLooper: Looper? = null

        @JvmStatic
        fun prepare() {
            prepare(true)
        }

        private fun prepare(quitAllowed: Boolean) {
            if (sThreadLocal.get() != null) {
                throw RuntimeException("Only one Looper may be created per thread")
            }
            sThreadLocal.set(Looper(quitAllowed))
        }

        @JvmStatic
        fun prepareMainLooper() {
            prepare(false)
            sMainLock.withLock {
                if (sMainLooper != null) {
                    throw IllegalStateException("The main Looper has already been prepared.")
                }
                sMainLooper = myLooper()
            }
        }

        @JvmStatic
        fun getMainLooper(): Looper? = sMainLock.withLock { sMainLooper }

        @JvmStatic
        fun myLooper(): Looper? = sThreadLocal.get()

        @JvmStatic
        fun myQueue(): MessageQueue {
            return myLooper()?.queue
                ?: throw RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.")
        }

        @JvmStatic
        fun loop() {
            val me = myLooper()
                ?: throw RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.")
            val queue = me.mQueue
            while (true) {
                val msg = queue.next() ?: return
                msg.target?.dispatchMessage(msg)
                msg.recycle()
            }
        }
    }
}
