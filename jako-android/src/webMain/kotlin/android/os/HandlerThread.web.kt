package android.os

import com.bennyhuo.jako.WebPlatform
import com.bennyhuo.jako.WebTaskScheduler

actual open class HandlerThread : Thread {
    private var threadPriority: Int
    private var tid: Int = -1
    private var _looper: Looper? = null
    private var unregisterPoller: (() -> Unit)? = null

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

    override fun start() {
        if (isAlive || _looper != null) {
            throw IllegalThreadStateException("Thread already started")
        }
        WebPlatform.markThreadRunning(this)
        WebPlatform.withCurrentThread(this) {
            tid = Process.myTid()
            Looper.prepare()
            _looper = Looper.myLooper()
            onLooperPrepared()
        }
        val cancel = WebTaskScheduler.registerPollingHook {
            val l = _looper ?: return@registerPollingHook false
            WebPlatform.withCurrentThread(this@HandlerThread) {
                val didWork = l.queue.pollSingleStep()
                if (l.queue.isQuittingAndEmpty()) {
                    finishQuitting()
                }
                didWork
            }
        }
        unregisterPoller = cancel
    }

    actual override fun run() {
        tid = Process.myTid()
        Looper.prepare()
        _looper = Looper.myLooper()
        onLooperPrepared()
        Looper.loop()
        tid = -1
    }

    actual open fun getLooper(): Looper? {
        if (!isAlive) return null
        return _looper
    }

    actual open fun quit(): Boolean {
        val l = getLooper() ?: return false
        l.quit()
        if (l.queue.isQuittingAndEmpty()) {
            finishQuitting()
        }
        return true
    }

    actual open fun quitSafely(): Boolean {
        val l = getLooper() ?: return false
        l.quitSafely()
        WebPlatform.withCurrentThread(this) {
            while (l.queue.pollSingleStep()) {
                // Drain due messages before finishing
            }
        }
        if (l.queue.isQuittingAndEmpty()) {
            finishQuitting()
        }
        return true
    }

    private fun finishQuitting() {
        unregisterPoller?.invoke()
        unregisterPoller = null
        _looper = null
        tid = -1
        WebPlatform.markThreadTerminated(this)
    }

    actual open fun getThreadId(): Int = tid
}
