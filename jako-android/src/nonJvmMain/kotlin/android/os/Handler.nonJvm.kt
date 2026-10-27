package android.os

import kotlin.concurrent.withLock

actual open class Handler {
    actual val looper: Looper
    internal val queue: MessageQueue
    internal val callback: Callback?
    internal val async: Boolean

    actual constructor() : this(null, false)

    actual constructor(callback: Callback?) : this(callback, false)

    actual constructor(looper: Looper) : this(looper, null, false)

    actual constructor(looper: Looper, callback: Callback?) : this(looper, callback, false)

    internal constructor(callback: Callback?, async: Boolean) {
        val l = Looper.myLooper()
            ?: throw RuntimeException("Can't create handler inside thread ${Thread.currentThread()} that has not called Looper.prepare()")
        this.looper = l
        this.queue = l.queue
        this.callback = callback
        this.async = async
    }

    internal constructor(looper: Looper, callback: Callback?, async: Boolean) {
        this.looper = looper
        this.queue = looper.queue
        this.callback = callback
        this.async = async
    }

    actual fun getLooper(): Looper = looper

    actual open fun handleMessage(msg: Message) {}

    actual fun dispatchMessage(msg: Message) {
        val cb = msg._callback
        if (cb != null) {
            cb.run()
        } else {
            val hcb = callback
            if (hcb != null) {
                if (hcb.handleMessage(msg)) {
                    return
                }
            }
            handleMessage(msg)
        }
    }

    actual open fun getMessageName(message: Message): String {
        val cb = message._callback
        if (cb != null) {
            return cb::class.simpleName ?: "Runnable"
        }
        return "0x${message.what.toString(16)}"
    }

    actual fun obtainMessage(): Message = Message.obtain(this)

    actual fun obtainMessage(what: Int): Message = Message.obtain(this, what)

    actual fun obtainMessage(what: Int, obj: Any?): Message = Message.obtain(this, what, obj)

    actual fun obtainMessage(what: Int, arg1: Int, arg2: Int): Message = Message.obtain(this, what, arg1, arg2)

    actual fun obtainMessage(what: Int, arg1: Int, arg2: Int, obj: Any?): Message =
        Message.obtain(this, what, arg1, arg2, obj)

    private fun getPostMessage(r: Runnable, token: Any? = null): Message {
        val m = Message.obtain()
        m._callback = r
        m.obj = token
        return m
    }

    actual fun post(r: Runnable): Boolean = sendMessageDelayed(getPostMessage(r), 0L)

    actual fun postAtTime(r: Runnable, uptimeMillis: Long): Boolean =
        sendMessageAtTime(getPostMessage(r), uptimeMillis)

    actual fun postAtTime(r: Runnable, token: Any?, uptimeMillis: Long): Boolean =
        sendMessageAtTime(getPostMessage(r, token), uptimeMillis)

    actual fun postDelayed(r: Runnable, delayMillis: Long): Boolean =
        sendMessageDelayed(getPostMessage(r), delayMillis)

    actual fun postDelayed(r: Runnable, token: Any?, delayMillis: Long): Boolean =
        sendMessageDelayed(getPostMessage(r, token), delayMillis)

    actual fun postAtFrontOfQueue(r: Runnable): Boolean =
        sendMessageAtFrontOfQueue(getPostMessage(r))

    actual fun removeCallbacks(r: Runnable) {
        queue.removeMessages(this, r, null)
    }

    actual fun removeCallbacks(r: Runnable, token: Any?) {
        queue.removeMessages(this, r, token)
    }

    actual fun sendMessage(msg: Message): Boolean = sendMessageDelayed(msg, 0L)

    actual fun sendEmptyMessage(what: Int): Boolean = sendEmptyMessageDelayed(what, 0L)

    actual fun sendEmptyMessageDelayed(what: Int, delayMillis: Long): Boolean {
        val msg = Message.obtain()
        msg.what = what
        return sendMessageDelayed(msg, delayMillis)
    }

    actual fun sendEmptyMessageAtTime(what: Int, uptimeMillis: Long): Boolean {
        val msg = Message.obtain()
        msg.what = what
        return sendMessageAtTime(msg, uptimeMillis)
    }

    actual fun sendMessageDelayed(msg: Message, delayMillis: Long): Boolean {
        val d = if (delayMillis < 0) 0L else delayMillis
        return sendMessageAtTime(msg, SystemClock.uptimeMillis() + d)
    }

    actual open fun sendMessageAtTime(msg: Message, uptimeMillis: Long): Boolean {
        return enqueueMessage(queue, msg, uptimeMillis)
    }

    actual fun sendMessageAtFrontOfQueue(msg: Message): Boolean {
        return enqueueMessage(queue, msg, 0L)
    }

    private fun enqueueMessage(queue: MessageQueue, msg: Message, uptimeMillis: Long): Boolean {
        msg.target = this
        if (async) {
            msg.isAsync = true
        }
        return queue.enqueueMessage(msg, uptimeMillis)
    }

    actual fun removeMessages(what: Int) {
        queue.removeMessages(this, what, null)
    }

    actual fun removeMessages(what: Int, `object`: Any?) {
        queue.removeMessages(this, what, `object`)
    }

    actual fun removeCallbacksAndMessages(token: Any?) {
        queue.removeCallbacksAndMessages(this, token)
    }

    actual fun hasMessages(what: Int): Boolean = queue.hasMessages(this, what, null)

    actual fun hasMessages(what: Int, `object`: Any?): Boolean = queue.hasMessages(this, what, `object`)

    actual fun hasCallbacks(r: Runnable): Boolean = queue.hasMessages(this, r, null)

    actual override fun toString(): String = "Handler (${this::class.simpleName})"

    actual fun interface Callback {
        actual fun handleMessage(msg: Message): Boolean
    }

    actual companion object {
        actual fun createAsync(looper: Looper): Handler = Handler(looper, null, true)
        actual fun createAsync(looper: Looper, callback: Callback): Handler = Handler(looper, callback, true)
    }
}
