package android.os

open class Handler {
    private val mLooper: Looper
    private val mQueue: MessageQueue
    private val mCallback: Callback?
    private val mAsynchronous: Boolean

    val looper: Looper
        get() = mLooper

    constructor() : this(null, false)

    constructor(callback: Callback?) : this(callback, false)

    constructor(looper: Looper) : this(looper, null, false)

    constructor(looper: Looper, callback: Callback?) : this(looper, callback, false)

    internal constructor(callback: Callback?, async: Boolean) {
        mLooper = Looper.myLooper()
            ?: throw RuntimeException("Can't create handler inside thread ${Thread.currentThread()} that has not called Looper.prepare()")
        mQueue = mLooper.queue
        mCallback = callback
        mAsynchronous = async
    }

    internal constructor(looper: Looper, callback: Callback?, async: Boolean) {
        mLooper = looper
        mQueue = looper.queue
        mCallback = callback
        mAsynchronous = async
    }

    open fun handleMessage(msg: Message) {}

    open fun dispatchMessage(msg: Message) {
        val callback = msg._callback
        if (callback != null) {
            callback.run()
        } else {
            if (mCallback != null) {
                if (mCallback.handleMessage(msg)) {
                    return
                }
            }
            handleMessage(msg)
        }
    }

    open fun getMessageName(message: Message): String {
        val cb = message._callback
        if (cb != null) return cb::class.qualifiedName ?: cb::class.simpleName ?: "Runnable"
        return "0x${message.what.toString(16)}"
    }

    fun obtainMessage(): Message = Message.obtain(this)

    fun obtainMessage(what: Int): Message = Message.obtain(this, what)

    fun obtainMessage(what: Int, obj: Any?): Message = Message.obtain(this, what, obj)

    fun obtainMessage(what: Int, arg1: Int, arg2: Int): Message = Message.obtain(this, what, arg1, arg2)

    fun obtainMessage(what: Int, arg1: Int, arg2: Int, obj: Any?): Message = Message.obtain(this, what, arg1, arg2, obj)

    fun post(r: Runnable): Boolean = sendMessageDelayed(getPostMessage(r), 0L)

    fun postAtTime(r: Runnable, uptimeMillis: Long): Boolean = sendMessageAtTime(getPostMessage(r), uptimeMillis)

    fun postAtTime(r: Runnable, token: Any?, uptimeMillis: Long): Boolean =
        sendMessageAtTime(getPostMessage(r, token), uptimeMillis)

    fun postDelayed(r: Runnable, delayMillis: Long): Boolean = sendMessageDelayed(getPostMessage(r), delayMillis)

    fun postDelayed(r: Runnable, token: Any?, delayMillis: Long): Boolean =
        sendMessageDelayed(getPostMessage(r, token), delayMillis)

    fun postAtFrontOfQueue(r: Runnable): Boolean = sendMessageAtFrontOfQueue(getPostMessage(r))

    fun removeCallbacks(r: Runnable) {
        mQueue.removeCallbacksAndMessages(this, r, null, matchRunnable = true)
    }

    fun removeCallbacks(r: Runnable, token: Any?) {
        mQueue.removeCallbacksAndMessages(this, r, token, matchRunnable = true)
    }

    fun sendMessage(msg: Message): Boolean = sendMessageDelayed(msg, 0L)

    fun sendEmptyMessage(what: Int): Boolean = sendEmptyMessageDelayed(what, 0L)

    fun sendEmptyMessageDelayed(what: Int, delayMillis: Long): Boolean {
        val msg = Message.obtain()
        msg.what = what
        return sendMessageDelayed(msg, delayMillis)
    }

    fun sendEmptyMessageAtTime(what: Int, uptimeMillis: Long): Boolean {
        val msg = Message.obtain()
        msg.what = what
        return sendMessageAtTime(msg, uptimeMillis)
    }

    fun sendMessageDelayed(msg: Message, delayMillis: Long): Boolean {
        val d = if (delayMillis < 0L) 0L else delayMillis
        return sendMessageAtTime(msg, SystemClock.uptimeMillis() + d)
    }

    open fun sendMessageAtTime(msg: Message, uptimeMillis: Long): Boolean {
        msg.target = this
        if (mAsynchronous) {
            msg.setAsynchronous(true)
        }
        return mQueue.enqueueMessage(msg, uptimeMillis)
    }

    fun sendMessageAtFrontOfQueue(msg: Message): Boolean {
        msg.target = this
        if (mAsynchronous) {
            msg.setAsynchronous(true)
        }
        return mQueue.enqueueMessage(msg, 0L)
    }

    fun removeMessages(what: Int) {
        mQueue.removeMessages(this, what, null)
    }

    fun removeMessages(what: Int, `object`: Any?) {
        mQueue.removeMessages(this, what, `object`)
    }

    fun removeCallbacksAndMessages(token: Any?) {
        mQueue.removeCallbacksAndMessages(this, null, token, matchRunnable = false)
    }

    fun hasMessages(what: Int): Boolean = mQueue.hasMessages(this, what, null)

    fun hasMessages(what: Int, `object`: Any?): Boolean = mQueue.hasMessages(this, what, `object`)

    fun hasCallbacks(r: Runnable): Boolean = mQueue.hasMessages(this, r, null)

    override fun toString(): String = "Handler (${this::class.simpleName})"

    private fun getPostMessage(r: Runnable, token: Any? = null): Message {
        val m = Message.obtain()
        m._callback = r
        m.obj = token
        return m
    }

    fun interface Callback {
        fun handleMessage(msg: Message): Boolean
    }

    companion object {
        @JvmStatic
        fun createAsync(looper: Looper): Handler = Handler(looper, null, true)

        @JvmStatic
        fun createAsync(looper: Looper, callback: Callback): Handler = Handler(looper, callback, true)
    }
}
