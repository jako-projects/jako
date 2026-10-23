package android.os

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual open class HandlerThread : Thread {
    private var threadPriority: Int
    private var tid: Int = -1
    private var _looper: Looper? = null
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    actual constructor(name: String) : super(name) {
        this.threadPriority = Process.THREAD_PRIORITY_DEFAULT
    }

    actual constructor(name: String, priority: Int) : super(name) {
        this.threadPriority = priority
    }

    actual val looper: Looper?
        get() = getLooper()

    actual val threadId: Int
        get() = getThreadId()

    protected actual open fun onLooperPrepared() {}

    actual override fun run() {
        tid = Process.myTid()
        Looper.prepare()
        lock.withLock {
            _looper = Looper.myLooper()
            condition.signalAll()
        }
        onLooperPrepared()
        Looper.loop()
        tid = -1
    }

    actual open fun getLooper(): Looper? {
        if (!isAlive) return null
        lock.withLock {
            while (isAlive && _looper == null) {
                try {
                    condition.await()
                } catch (_: InterruptedException) {
                }
            }
            return _looper
        }
    }

    actual open fun quit(): Boolean {
        val l = getLooper() ?: return false
        l.quit()
        return true
    }

    actual open fun quitSafely(): Boolean {
        val l = getLooper() ?: return false
        l.quitSafely()
        return true
    }

    actual open fun getThreadId(): Int = tid
}
