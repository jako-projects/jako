package android.os

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
