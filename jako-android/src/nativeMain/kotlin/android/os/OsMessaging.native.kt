package android.os

import java.lang.ThreadLocal
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual final class Message actual constructor() : Parcelable {
    actual var what: Int = 0
    actual var arg1: Int = 0
    actual var arg2: Int = 0
    actual var obj: Any? = null
    actual var sendingUid: Int = -1
    actual var target: Handler? = null
    internal var _callback: Runnable? = null
    internal var _when: Long = 0L
    internal var _data: Bundle? = null
    internal var isAsync: Boolean = false
    internal var inUse: Boolean = false
    internal var next: Message? = null

    actual val callback: Runnable?
        get() = _callback

    actual var data: Bundle
        get() = getData()
        set(value) = setData(value)

    actual fun getWhen(): Long = _when

    actual fun getTarget(): Handler? = target

    actual fun setTarget(target: Handler?) {
        this.target = target
    }

    actual fun getCallback(): Runnable? = _callback

    actual fun getData(): Bundle {
        var b = _data
        if (b == null) {
            b = Bundle()
            _data = b
        }
        return b
    }

    actual fun peekData(): Bundle? = _data

    actual fun setData(data: Bundle?) {
        _data = data
    }

    actual fun recycle() {
        what = 0
        arg1 = 0
        arg2 = 0
        obj = null
        sendingUid = -1
        _when = 0L
        target = null
        _callback = null
        _data = null
        isAsync = false
        inUse = false
        next = null
    }

    actual fun copyFrom(o: Message) {
        this.what = o.what
        this.arg1 = o.arg1
        this.arg2 = o.arg2
        this.obj = o.obj
        this.sendingUid = o.sendingUid
        val d = o._data
        this._data = if (d != null) Bundle(d) else null
    }

    actual fun sendToTarget() {
        val t = target ?: throw NullPointerException("Message target is null")
        t.sendMessage(this)
    }

    actual fun isAsynchronous(): Boolean = isAsync

    actual fun setAsynchronous(async: Boolean) {
        isAsync = async
    }

    actual override fun describeContents(): Int = 0

    actual override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(what)
        dest.writeInt(arg1)
        dest.writeInt(arg2)
        dest.writeLong(_when)
        dest.writeBundle(_data)
    }

    actual override fun toString(): String {
        val sb = StringBuilder("{ when=")
        sb.append(_when - SystemClock.uptimeMillis()).append("ms")
        if (target != null) {
            if (_callback != null) {
                sb.append(" callback=").append(_callback!!::class.simpleName)
            } else {
                sb.append(" what=").append(what)
            }
            if (arg1 != 0) sb.append(" arg1=").append(arg1)
            if (arg2 != 0) sb.append(" arg2=").append(arg2)
            if (obj != null) sb.append(" obj=").append(obj)
            sb.append(" target=").append(target!!::class.simpleName)
        } else {
            sb.append(" barrier=").append(arg1)
        }
        sb.append(" }")
        return sb.toString()
    }

    actual companion object {
        actual fun obtain(): Message = Message()

        actual fun obtain(orig: Message): Message {
            val m = obtain()
            m.what = orig.what
            m.arg1 = orig.arg1
            m.arg2 = orig.arg2
            m.obj = orig.obj
            m.sendingUid = orig.sendingUid
            if (orig._data != null) {
                m._data = Bundle(orig._data)
            }
            m.target = orig.target
            m._callback = orig._callback
            return m
        }

        actual fun obtain(h: Handler): Message {
            val m = obtain()
            m.target = h
            return m
        }

        actual fun obtain(h: Handler, callback: Runnable): Message {
            val m = obtain()
            m.target = h
            m._callback = callback
            return m
        }

        actual fun obtain(h: Handler, what: Int): Message {
            val m = obtain()
            m.target = h
            m.what = what
            return m
        }

        actual fun obtain(h: Handler, what: Int, obj: Any?): Message {
            val m = obtain()
            m.target = h
            m.what = what
            m.obj = obj
            return m
        }

        actual fun obtain(h: Handler, what: Int, arg1: Int, arg2: Int): Message {
            val m = obtain()
            m.target = h
            m.what = what
            m.arg1 = arg1
            m.arg2 = arg2
            return m
        }

        actual fun obtain(h: Handler, what: Int, arg1: Int, arg2: Int, obj: Any?): Message {
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

actual final class MessageQueue internal constructor(private val quitAllowed: Boolean) {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private var messages: Message? = null
    private val idleHandlers = ArrayList<IdleHandler>()
    private var quitting = false

    actual val isIdle: Boolean
        get() = isIdle()

    actual fun isIdle(): Boolean = lock.withLock {
        val now = SystemClock.uptimeMillis()
        messages == null || now < messages!!._when
    }

    actual fun addIdleHandler(handler: IdleHandler) {
        lock.withLock {
            idleHandlers.add(handler)
        }
    }

    actual fun removeIdleHandler(handler: IdleHandler) {
        lock.withLock {
            idleHandlers.remove(handler)
        }
    }

    internal fun next(): Message? {
        var pendingIdleHandlerCount = -1
        var nextPollTimeoutMillis = 0L

        while (true) {
            var idleHandlersSnapshot: List<IdleHandler>? = null
            lock.withLock {
                if (nextPollTimeoutMillis > 0) {
                    condition.awaitNanos(nextPollTimeoutMillis * 1_000_000L)
                } else if (nextPollTimeoutMillis < 0) {
                    condition.await()
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

                if (quitting) {
                    return null
                }

                if (pendingIdleHandlerCount < 0 && (messages == null || now < messages!!._when)) {
                    pendingIdleHandlerCount = idleHandlers.size
                }
                if (pendingIdleHandlerCount <= 0) {
                    return@withLock
                }
                idleHandlersSnapshot = ArrayList(idleHandlers)
            }

            val handlers = idleHandlersSnapshot
            if (handlers != null) {
                for (idler in handlers) {
                    val keep = try {
                        idler.queueIdle()
                    } catch (_: Throwable) {
                        false
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
    }

    internal fun quit(safe: Boolean) {
        if (!quitAllowed) {
            throw IllegalStateException("Main thread not allowed to quit.")
        }
        lock.withLock {
            if (quitting) return
            quitting = true
            if (safe) {
                removeAllFutureMessagesLocked()
            } else {
                removeAllMessagesLocked()
            }
            condition.signalAll()
        }
    }

    internal fun enqueueMessage(msg: Message, whenMillis: Long): Boolean {
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
            msg._when = whenMillis
            var p = messages
            if (p == null || whenMillis == 0L || whenMillis < p._when) {
                msg.next = p
                messages = msg
            } else {
                var prev: Message
                while (true) {
                    prev = p!!
                    p = p.next
                    if (p == null || whenMillis < p._when) {
                        break
                    }
                }
                msg.next = p
                prev.next = msg
            }
            condition.signalAll()
            return true
        }
    }

    internal fun hasMessages(h: Handler, what: Int, `object`: Any?): Boolean = lock.withLock {
        var p = messages
        while (p != null) {
            if (p.target === h && p.what == what && (`object` == null || p.obj == `object`)) {
                return true
            }
            p = p.next
        }
        false
    }

    internal fun hasMessages(h: Handler, r: Runnable, `object`: Any?): Boolean = lock.withLock {
        var p = messages
        while (p != null) {
            if (p.target === h && p._callback === r && (`object` == null || p.obj == `object`)) {
                return true
            }
            p = p.next
        }
        false
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

    internal fun removeMessages(h: Handler, r: Runnable, `object`: Any?) {
        lock.withLock {
            var p = messages
            while (p != null && p.target === h && p._callback === r && (`object` == null || p.obj == `object`)) {
                val n = p.next
                messages = n
                p.recycle()
                p = n
            }
            while (p != null) {
                val n = p.next
                if (n != null && n.target === h && n._callback === r && (`object` == null || n.obj == `object`)) {
                    val nn = n.next
                    n.recycle()
                    p.next = nn
                    continue
                }
                p = n
            }
        }
    }

    internal fun removeCallbacksAndMessages(h: Handler, `object`: Any?) {
        lock.withLock {
            var p = messages
            while (p != null && p.target === h && (`object` == null || p.obj == `object`)) {
                val n = p.next
                messages = n
                p.recycle()
                p = n
            }
            while (p != null) {
                val n = p.next
                if (n != null && n.target === h && (`object` == null || n.obj == `object`)) {
                    val nn = n.next
                    n.recycle()
                    p.next = nn
                    continue
                }
                p = n
            }
        }
    }

    private fun removeAllMessagesLocked() {
        var p = messages
        while (p != null) {
            val n = p.next
            p.recycle()
            p = n
        }
        messages = null
    }

    private fun removeAllFutureMessagesLocked() {
        val now = SystemClock.uptimeMillis()
        var p = messages
        if (p != null) {
            if (p._when > now) {
                removeAllMessagesLocked()
            } else {
                while (true) {
                    val n = p!!.next ?: return
                    if (n._when > now) break
                    p = n
                }
                var n = p!!.next
                p.next = null
                while (n != null) {
                    val next = n.next
                    n.recycle()
                    n = next
                }
            }
        }
    }

    actual fun interface IdleHandler {
        actual fun queueIdle(): Boolean
    }
}

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
        private val threadLocal = ThreadLocal<Looper>()
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

actual open class HandlerThread : Thread {
    private var priority: Int
    private var tid: Int = -1
    private var _looper: Looper? = null
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    actual constructor(name: String) : super(name) {
        this.priority = Process.THREAD_PRIORITY_DEFAULT
    }

    actual constructor(name: String, priority: Int) : super(name) {
        this.priority = priority
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
