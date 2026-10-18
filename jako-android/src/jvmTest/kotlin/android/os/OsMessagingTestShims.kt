package android.os

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Test-only JVM shims for [Message], [MessageQueue], [Looper], [Handler], and [HandlerThread]
 * (since `android-all`'s `MessageQueue` calls C++ epoll JNI methods `nativeInit`/`nativePollOnce`/`nativeWake`).
 */
class Message() : Parcelable {
    @JvmField
    var what: Int = 0
    @JvmField
    var arg1: Int = 0
    @JvmField
    var arg2: Int = 0
    @JvmField
    var obj: Any? = null
    @JvmField
    var sendingUid: Int = -1
    @JvmField
    var target: Handler? = null
    internal var _callback: Runnable? = null
    internal var _when: Long = 0L
    internal var _data: Bundle? = null
    internal var isAsync: Boolean = false
    internal var inUse: Boolean = false
    internal var next: Message? = null

    val callback: Runnable?
        get() = _callback

    var data: Bundle
        get() {
            var b = _data
            if (b == null) {
                b = Bundle()
                _data = b
            }
            return b
        }
        set(value) {
            _data = value
        }

    val `when`: Long
        get() = _when

    fun recycle() {
        clearForRecycle()
        sPoolSync.withLock {
            if (sPoolSize < MAX_POOL_SIZE) {
                next = sPool
                sPool = this
                sPoolSize++
            }
        }
    }

    internal fun clearForRecycle() {
        what = 0
        arg1 = 0
        arg2 = 0
        obj = null
        sendingUid = -1
        target = null
        _callback = null
        _when = 0L
        _data = null
        isAsync = false
        inUse = false
        next = null
    }

    fun copyFrom(o: Message) {
        this.what = o.what
        this.arg1 = o.arg1
        this.arg2 = o.arg2
        this.obj = o.obj
        this.sendingUid = o.sendingUid
        this.isAsync = o.isAsync
        this._data = o._data?.let { Bundle(it) }
    }

    fun peekData(): Bundle? = _data

    fun sendToTarget() {
        target?.sendMessage(this) ?: throw IllegalStateException("Message has no target Handler")
    }

    fun isAsynchronous(): Boolean = isAsync

    fun setAsynchronous(async: Boolean) {
        isAsync = async
    }

    override fun toString(): String {
        val b = StringBuilder("{ when=")
        b.append(_when)
        if (target != null) {
            if (_callback != null) {
                b.append(" callback=").append(_callback!!::class.simpleName)
            } else {
                b.append(" what=").append(what)
            }
            if (arg1 != 0) b.append(" arg1=").append(arg1)
            if (arg2 != 0) b.append(" arg2=").append(arg2)
            if (obj != null) b.append(" obj=").append(obj)
            b.append(" target=").append(target!!::class.simpleName)
        } else {
            b.append(" barrier=").append(arg1)
        }
        b.append(" }")
        return b.toString()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(what)
        dest.writeInt(arg1)
        dest.writeInt(arg2)
        dest.writeLong(_when)
        dest.writeBundle(_data)
    }

    companion object {
        private val sPoolSync = ReentrantLock()
        private var sPool: Message? = null
        private var sPoolSize = 0
        private const val MAX_POOL_SIZE = 50

        @JvmStatic
        fun obtain(): Message {
            sPoolSync.withLock {
                val m = sPool
                if (m != null) {
                    sPool = m.next
                    m.next = null
                    m.inUse = false
                    sPoolSize--
                    return m
                }
            }
            return Message()
        }

        @JvmStatic
        fun obtain(orig: Message): Message {
            val m = obtain()
            m.what = orig.what
            m.arg1 = orig.arg1
            m.arg2 = orig.arg2
            m.obj = orig.obj
            m.sendingUid = orig.sendingUid
            m.target = orig.target
            m._callback = orig._callback
            m.isAsync = orig.isAsync
            if (orig._data != null) {
                m._data = Bundle(orig._data!!)
            }
            return m
        }

        @JvmStatic
        fun obtain(h: Handler): Message {
            val m = obtain()
            m.target = h
            return m
        }

        @JvmStatic
        fun obtain(h: Handler, callback: Runnable): Message {
            val m = obtain()
            m.target = h
            m._callback = callback
            return m
        }

        @JvmStatic
        fun obtain(h: Handler, what: Int): Message {
            val m = obtain()
            m.target = h
            m.what = what
            return m
        }

        @JvmStatic
        fun obtain(h: Handler, what: Int, obj: Any?): Message {
            val m = obtain()
            m.target = h
            m.what = what
            m.obj = obj
            return m
        }

        @JvmStatic
        fun obtain(h: Handler, what: Int, arg1: Int, arg2: Int): Message {
            val m = obtain()
            m.target = h
            m.what = what
            m.arg1 = arg1
            m.arg2 = arg2
            return m
        }

        @JvmStatic
        fun obtain(h: Handler, what: Int, arg1: Int, arg2: Int, obj: Any?): Message {
            val m = obtain()
            m.target = h
            m.what = what
            m.arg1 = arg1
            m.arg2 = arg2
            m.obj = obj
            return m
        }
    }
}

class MessageQueue internal constructor(private val quitAllowed: Boolean) {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private var messages: Message? = null
    private val idleHandlers = ArrayList<IdleHandler>()
    private var quitting = false

    val isIdle: Boolean
        get() {
            lock.withLock {
                val now = SystemClock.uptimeMillis()
                val head = messages
                return head == null || now < head._when
            }
        }

    fun addIdleHandler(handler: IdleHandler) {
        lock.withLock {
            idleHandlers.add(handler)
        }
    }

    fun removeIdleHandler(handler: IdleHandler) {
        lock.withLock {
            idleHandlers.remove(handler)
        }
    }

    internal fun next(): Message? {
        var pendingIdleHandlerCount = -1
        var nextPollTimeoutMillis = 0L

        while (true) {
            var idleSnapshot: List<IdleHandler>? = null
            lock.withLock {
                if (nextPollTimeoutMillis > 0L) {
                    condition.awaitNanos(nextPollTimeoutMillis * 1_000_000L)
                } else if (nextPollTimeoutMillis < 0L) {
                    if (!quitting && messages == null) {
                        condition.await()
                    }
                }

                val now = SystemClock.uptimeMillis()
                val msg = messages
                if (msg != null) {
                    if (now < msg._when) {
                        nextPollTimeoutMillis = msg._when - now
                    } else {
                        messages = msg.next
                        msg.next = null
                        msg.inUse = true
                        return msg
                    }
                } else {
                    nextPollTimeoutMillis = -1L
                }

                if (quitting) return null

                if (pendingIdleHandlerCount < 0 && (messages == null || now < messages!!._when)) {
                    pendingIdleHandlerCount = idleHandlers.size
                }
                if (pendingIdleHandlerCount <= 0) {
                    continue
                }
                idleSnapshot = ArrayList(idleHandlers)
            }

            val snapshot = idleSnapshot ?: continue
            for (idler in snapshot) {
                var keep = false
                try {
                    keep = idler.queueIdle()
                } catch (_: Throwable) {
                }
                if (!keep) {
                    lock.withLock {
                        idleHandlers.remove(idler)
                    }
                }
            }
            pendingIdleHandlerCount = 0
            nextPollTimeoutMillis = 0L
        }
    }

    internal fun enqueueMessage(msg: Message, `when`: Long): Boolean {
        if (msg.target == null) {
            throw IllegalArgumentException("Message must have a target.")
        }
        lock.withLock {
            if (msg.inUse) {
                throw IllegalStateException("$msg This message is already in use.")
            }
            if (quitting) {
                msg.recycle()
                return false
            }
            msg.inUse = true
            msg._when = `when`
            var p = messages
            if (p == null || `when` == 0L || `when` < p._when) {
                msg.next = p
                messages = msg
                condition.signalAll()
            } else {
                var prev: Message?
                while (true) {
                    prev = p
                    p = p?.next
                    if (p == null || `when` < p._when) {
                        break
                    }
                }
                msg.next = p
                prev!!.next = msg
                condition.signalAll()
            }
            return true
        }
    }

    internal fun hasMessages(h: Handler, what: Int, `object`: Any?): Boolean {
        lock.withLock {
            var p = messages
            while (p != null) {
                if (p.target === h && p.what == what && (`object` == null || p.obj == `object`)) {
                    return true
                }
                p = p.next
            }
            return false
        }
    }

    internal fun hasMessages(h: Handler, r: Runnable?, `object`: Any?): Boolean {
        lock.withLock {
            var p = messages
            while (p != null) {
                if (p.target === h && p._callback === r && (`object` == null || p.obj == `object`)) {
                    return true
                }
                p = p.next
            }
            return false
        }
    }

    internal fun removeMessages(h: Handler, what: Int, `object`: Any?) {
        lock.withLock {
            var p = messages
            while (p != null && p.target === h && p.what == what && (`object` == null || p.obj == `object`)) {
                val n = p.next
                messages = n
                p.recycle()
                p = n
            }
            while (p != null) {
                val n = p.next
                if (n != null && n.target === h && n.what == what && (`object` == null || n.obj == `object`)) {
                    val nn = n.next
                    n.recycle()
                    p.next = nn
                    continue
                }
                p = n
            }
        }
    }

    internal fun removeCallbacksAndMessages(h: Handler, r: Runnable?, `object`: Any?, matchRunnable: Boolean) {
        lock.withLock {
            var p = messages
            while (p != null && p.target === h &&
                (!matchRunnable || p._callback === r) &&
                (`object` == null || p.obj === `object`)
            ) {
                val n = p.next
                messages = n
                p.recycle()
                p = n
            }
            while (p != null) {
                val n = p.next
                if (n != null && n.target === h &&
                    (!matchRunnable || n._callback === r) &&
                    (`object` == null || n.obj === `object`)
                ) {
                    val nn = n.next
                    n.recycle()
                    p.next = nn
                    continue
                }
                p = n
            }
        }
    }

    internal fun quit(safe: Boolean) {
        if (!quitAllowed) {
            throw IllegalStateException("Main thread not allowed to quit.")
        }
        lock.withLock {
            if (quitting) return
            quitting = true
            if (safe) {
                val now = SystemClock.uptimeMillis()
                var p = messages
                var prev: Message? = null
                while (p != null) {
                    if (p._when > now) break
                    prev = p
                    p = p.next
                }
                if (prev != null) {
                    prev.next = null
                } else {
                    messages = null
                }
                while (p != null) {
                    val next = p.next
                    p.recycle()
                    p = next
                }
            } else {
                var p = messages
                while (p != null) {
                    val next = p.next
                    p.recycle()
                    p = next
                }
                messages = null
            }
            condition.signalAll()
        }
    }

    fun interface IdleHandler {
        fun queueIdle(): Boolean
    }
}

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
