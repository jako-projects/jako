package android.os

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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
