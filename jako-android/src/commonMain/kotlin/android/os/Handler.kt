@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

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
