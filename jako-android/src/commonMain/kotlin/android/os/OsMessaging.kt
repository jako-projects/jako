@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

/**
 * Defines a message containing a description and arbitrary data object that can be
 * sent to a [Handler].
 */
expect final class Message() : Parcelable {
    var what: Int
    var arg1: Int
    var arg2: Int
    var obj: Any?
    var sendingUid: Int
    var target: Handler?
    val callback: Runnable?
    var data: Bundle

    fun getWhen(): Long
    fun getTarget(): Handler?
    fun setTarget(target: Handler?)
    fun getCallback(): Runnable?
    fun getData(): Bundle
    fun peekData(): Bundle?
    fun setData(data: Bundle?)

    fun recycle()
    fun copyFrom(o: Message)
    fun sendToTarget()
    fun isAsynchronous(): Boolean
    fun setAsynchronous(async: Boolean)

    override fun describeContents(): Int
    override fun writeToParcel(dest: Parcel, flags: Int)
    override fun toString(): String

    companion object {
        fun obtain(): Message
        fun obtain(orig: Message): Message
        fun obtain(h: Handler): Message
        fun obtain(h: Handler, callback: Runnable): Message
        fun obtain(h: Handler, what: Int): Message
        fun obtain(h: Handler, what: Int, obj: Any?): Message
        fun obtain(h: Handler, what: Int, arg1: Int, arg2: Int): Message
        fun obtain(h: Handler, what: Int, arg1: Int, arg2: Int, obj: Any?): Message
    }
}

/**
 * Low-level class holding the list of messages to be dispatched by a [Looper].
 */
expect final class MessageQueue {
    val isIdle: Boolean

    fun isIdle(): Boolean
    fun addIdleHandler(handler: IdleHandler)
    fun removeIdleHandler(handler: IdleHandler)

    fun interface IdleHandler {
        fun queueIdle(): Boolean
    }
}

/**
 * Class used to run a message loop for a thread.
 */
expect final class Looper {
    val thread: Thread
    val queue: MessageQueue
    val isCurrentThread: Boolean

    fun getThread(): Thread
    fun getQueue(): MessageQueue
    fun isCurrentThread(): Boolean
    fun quit()
    fun quitSafely()
    override fun toString(): String

    companion object {
        fun prepare()
        fun prepareMainLooper()
        fun getMainLooper(): Looper?
        fun myLooper(): Looper?
        fun myQueue(): MessageQueue
        fun loop()
    }
}

/**
 * A Handler allows you to send and process [Message] and Runnable
 * objects associated with a thread's [MessageQueue].
 */
expect open class Handler {
    constructor()
    constructor(callback: Callback?)
    constructor(looper: Looper)
    constructor(looper: Looper, callback: Callback?)

    val looper: Looper

    fun getLooper(): Looper
    open fun handleMessage(msg: Message)
    fun dispatchMessage(msg: Message)
    open fun getMessageName(message: Message): String

    fun obtainMessage(): Message
    fun obtainMessage(what: Int): Message
    fun obtainMessage(what: Int, obj: Any?): Message
    fun obtainMessage(what: Int, arg1: Int, arg2: Int): Message
    fun obtainMessage(what: Int, arg1: Int, arg2: Int, obj: Any?): Message

    fun post(r: Runnable): Boolean
    fun postAtTime(r: Runnable, uptimeMillis: Long): Boolean
    fun postAtTime(r: Runnable, token: Any?, uptimeMillis: Long): Boolean
    fun postDelayed(r: Runnable, delayMillis: Long): Boolean
    fun postDelayed(r: Runnable, token: Any?, delayMillis: Long): Boolean
    fun postAtFrontOfQueue(r: Runnable): Boolean
    fun removeCallbacks(r: Runnable)
    fun removeCallbacks(r: Runnable, token: Any?)

    fun sendMessage(msg: Message): Boolean
    fun sendEmptyMessage(what: Int): Boolean
    fun sendEmptyMessageDelayed(what: Int, delayMillis: Long): Boolean
    fun sendEmptyMessageAtTime(what: Int, uptimeMillis: Long): Boolean
    fun sendMessageDelayed(msg: Message, delayMillis: Long): Boolean
    open fun sendMessageAtTime(msg: Message, uptimeMillis: Long): Boolean
    fun sendMessageAtFrontOfQueue(msg: Message): Boolean

    fun removeMessages(what: Int)
    fun removeMessages(what: Int, `object`: Any?)
    fun removeCallbacksAndMessages(token: Any?)
    fun hasMessages(what: Int): Boolean
    fun hasMessages(what: Int, `object`: Any?): Boolean
    fun hasCallbacks(r: Runnable): Boolean

    override fun toString(): String

    fun interface Callback {
        fun handleMessage(msg: Message): Boolean
    }

    companion object {
        fun createAsync(looper: Looper): Handler
        fun createAsync(looper: Looper, callback: Callback): Handler
    }
}

/**
 * Handy class for starting a new thread that has a looper.
 */
expect open class HandlerThread : Thread {
    constructor(name: String)
    constructor(name: String, priority: Int)

    val looper: Looper?
    val threadId: Int

    protected open fun onLooperPrepared()
    override fun run()
    open fun getLooper(): Looper?
    open fun quit(): Boolean
    open fun quitSafely(): Boolean
    open fun getThreadId(): Int
}
