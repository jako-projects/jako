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
