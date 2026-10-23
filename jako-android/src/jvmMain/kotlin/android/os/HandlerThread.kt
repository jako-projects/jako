package android.os

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

open class HandlerThread : Thread {
    private var mPriority: Int
    private var mTid: Int = -1
    private var mLooper: Looper? = null
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    constructor(name: String) : super(name) {
        this.mPriority = Process.THREAD_PRIORITY_DEFAULT
    }

    constructor(name: String, priority: Int) : super(name) {
        this.mPriority = priority
    }

    open val looper: Looper?
        get() {
            if (!isAlive) return null
            lock.withLock {
                while (isAlive && mLooper == null) {
                    try {
                        condition.await()
                    } catch (_: InterruptedException) {
                    }
                }
                return mLooper
            }
        }

    open val threadId: Int
        get() = mTid

    protected open fun onLooperPrepared() {}

    override fun run() {
        mTid = Process.myTid()
        Looper.prepare()
        lock.withLock {
            mLooper = Looper.myLooper()
            condition.signalAll()
        }
        onLooperPrepared()
        Looper.loop()
        mTid = -1
    }

    open fun quit(): Boolean {
        val l = looper ?: return false
        l.quit()
        return true
    }

    open fun quitSafely(): Boolean {
        val l = looper ?: return false
        l.quitSafely()
        return true
    }
}
