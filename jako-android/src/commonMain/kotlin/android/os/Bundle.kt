@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

import java.io.Serializable
import java.lang.Cloneable

/**
 * A mapping from String keys to various [Parcelable] values.
 */
expect final class Bundle : BaseBundle, Cloneable, Parcelable {
    constructor()
    constructor(capacity: Int)
    constructor(b: Bundle?)
    constructor(b: PersistableBundle?)

    fun clone(): Any
    fun deepCopy(): Bundle
    fun putAll(bundle: Bundle)
    fun hasFileDescriptors(): Boolean

    fun putByte(key: String?, value: Byte)
    fun putChar(key: String?, value: Char)
    fun putShort(key: String?, value: Short)
    fun putFloat(key: String?, value: Float)
    fun putCharSequence(key: String?, value: CharSequence?)
    fun putBundle(key: String?, value: Bundle?)
    fun putParcelable(key: String?, value: Parcelable?)
    fun putSerializable(key: String?, value: Serializable?)

    fun putByteArray(key: String?, value: ByteArray?)
    fun putShortArray(key: String?, value: ShortArray?)
    fun putCharArray(key: String?, value: CharArray?)
    fun putFloatArray(key: String?, value: FloatArray?)
    fun putCharSequenceArray(key: String?, value: Array<CharSequence?>?)

    fun putIntegerArrayList(key: String?, value: ArrayList<Int>?)
    fun putStringArrayList(key: String?, value: ArrayList<String>?)
    fun putCharSequenceArrayList(key: String?, value: ArrayList<CharSequence>?)

    fun getByte(key: String?): Byte
    fun getByte(key: String?, defaultValue: Byte): Byte
    fun getChar(key: String?): Char
    fun getChar(key: String?, defaultValue: Char): Char
    fun getShort(key: String?): Short
    fun getShort(key: String?, defaultValue: Short): Short
    fun getFloat(key: String?): Float
    fun getFloat(key: String?, defaultValue: Float): Float
    fun getCharSequence(key: String?): CharSequence?
    fun getCharSequence(key: String?, defaultValue: CharSequence?): CharSequence?
    fun getBundle(key: String?): Bundle?
    fun <T : Parcelable> getParcelable(key: String?): T?
    fun getSerializable(key: String?): Serializable?

    fun getByteArray(key: String?): ByteArray?
    fun getShortArray(key: String?): ShortArray?
    fun getCharArray(key: String?): CharArray?
    fun getFloatArray(key: String?): FloatArray?
    fun getCharSequenceArray(key: String?): Array<CharSequence?>?

    fun getIntegerArrayList(key: String?): ArrayList<Int>?
    fun getStringArrayList(key: String?): ArrayList<String>?
    fun getCharSequenceArrayList(key: String?): ArrayList<CharSequence>?

    override fun describeContents(): Int
    override fun writeToParcel(dest: Parcel, flags: Int)
    override fun toString(): String

    companion object {
        val EMPTY: Bundle
        val CREATOR: Parcelable.Creator<Bundle>
    }
}
