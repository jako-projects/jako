@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

import java.io.Serializable
import java.lang.Cloneable

/**
 * Core timekeeping facilities matching `android.os.SystemClock`.
 */
expect final class SystemClock {
    companion object {
        fun sleep(ms: Long)
        fun uptimeMillis(): Long
        fun elapsedRealtime(): Long
        fun elapsedRealtimeNanos(): Long
    }
}

/**
 * Information about the current build, extracted from system properties.
 */
expect open class Build() {
    class VERSION() {
        companion object {
            val INCREMENTAL: String
            val RELEASE: String
            val SDK: String
            val SDK_INT: Int
            val CODENAME: String
        }
    }

    class VERSION_CODES() {
        companion object {
            val CUR_DEVELOPMENT: Int
            val BASE: Int
            val BASE_1_1: Int
            val CUPCAKE: Int
            val DONUT: Int
            val ECLAIR: Int
            val ECLAIR_0_1: Int
            val ECLAIR_MR1: Int
            val FROYO: Int
            val GINGERBREAD: Int
            val GINGERBREAD_MR1: Int
            val HONEYCOMB: Int
            val HONEYCOMB_MR1: Int
            val HONEYCOMB_MR2: Int
            val ICE_CREAM_SANDWICH: Int
            val ICE_CREAM_SANDWICH_MR1: Int
            val JELLY_BEAN: Int
            val JELLY_BEAN_MR1: Int
            val JELLY_BEAN_MR2: Int
            val KITKAT: Int
            val KITKAT_WATCH: Int
            val LOLLIPOP: Int
            val LOLLIPOP_MR1: Int
            val M: Int
            val N: Int
            val N_MR1: Int
            val O: Int
            val O_MR1: Int
            val P: Int
            val Q: Int
        }
    }

    companion object {
        val UNKNOWN: String
        val ID: String
        val DISPLAY: String
        val PRODUCT: String
        val DEVICE: String
        val BOARD: String
        val MANUFACTURER: String
        val BRAND: String
        val MODEL: String
        val BOOTLOADER: String
        val HARDWARE: String
        val TYPE: String
        val TAGS: String
        val FINGERPRINT: String
        val USER: String
        val HOST: String
    }
}

/**
 * Tools for managing OS processes.
 */
expect open class Process() {
    companion object {
        val THREAD_PRIORITY_DEFAULT: Int
        val THREAD_PRIORITY_LOWEST: Int
        val THREAD_PRIORITY_BACKGROUND: Int
        val THREAD_PRIORITY_FOREGROUND: Int
        val THREAD_PRIORITY_DISPLAY: Int
        val THREAD_PRIORITY_URGENT_DISPLAY: Int
        val THREAD_PRIORITY_VIDEO: Int
        val THREAD_PRIORITY_AUDIO: Int
        val THREAD_PRIORITY_URGENT_AUDIO: Int
        val THREAD_PRIORITY_MORE_FAVORABLE: Int
        val THREAD_PRIORITY_LESS_FAVORABLE: Int

        fun myPid(): Int
        fun myTid(): Int
        fun myUid(): Int
        fun is64Bit(): Boolean
    }
}

/**
 * Interface for classes whose instances can be written to and restored from a [Parcel].
 */
expect interface Parcelable {
    fun describeContents(): Int
    fun writeToParcel(dest: Parcel, flags: Int)

    interface Creator<T> {
        fun createFromParcel(source: Parcel): T
        fun newArray(size: Int): Array<T?>
    }

    companion object {
        val PARCELABLE_WRITE_RETURN_VALUE: Int
        val CONTENTS_FILE_DESCRIPTOR: Int
    }
}

/**
 * Container for a message (data and object references) that can be sent through an IBinder
 * or serialized in memory.
 */
expect final class Parcel {
    fun recycle()
    fun dataPosition(): Int
    fun setDataPosition(pos: Int)
    fun dataSize(): Int
    fun writeByte(valByte: Byte)
    fun readByte(): Byte
    fun writeInt(valInt: Int)
    fun readInt(): Int
    fun writeLong(valLong: Long)
    fun readLong(): Long
    fun writeFloat(valFloat: Float)
    fun readFloat(): Float
    fun writeDouble(valDouble: Double)
    fun readDouble(): Double
    fun writeString(valStr: String?)
    fun readString(): String?
    fun writeByteArray(b: ByteArray?)
    fun createByteArray(): ByteArray?
    fun writeIntArray(valArray: IntArray?)
    fun createIntArray(): IntArray?
    fun writeLongArray(valArray: LongArray?)
    fun createLongArray(): LongArray?
    fun writeStringArray(valArray: Array<String?>?)
    fun createStringArray(): Array<String?>?
    fun writeBundle(valBundle: Bundle?)
    fun readBundle(): Bundle?
    fun marshall(): ByteArray
    fun unmarshall(data: ByteArray, offset: Int, length: Int)

    companion object {
        fun obtain(): Parcel
    }
}

/**
 * A mapping from String keys to values of various types.
 */
expect open class BaseBundle {
    val isEmpty: Boolean

    fun size(): Int
    fun isEmpty(): Boolean
    fun clear()
    fun containsKey(key: String?): Boolean
    operator fun get(key: String?): Any?
    fun remove(key: String?)
    fun putAll(bundle: PersistableBundle)
    fun keySet(): MutableSet<String>

    fun putBoolean(key: String?, value: Boolean)
    fun putInt(key: String?, value: Int)
    fun putLong(key: String?, value: Long)
    fun putDouble(key: String?, value: Double)
    fun putString(key: String?, value: String?)
    fun putBooleanArray(key: String?, value: BooleanArray?)
    fun putIntArray(key: String?, value: IntArray?)
    fun putLongArray(key: String?, value: LongArray?)
    fun putDoubleArray(key: String?, value: DoubleArray?)
    fun putStringArray(key: String?, value: Array<String?>?)

    fun getBoolean(key: String?): Boolean
    fun getBoolean(key: String?, defaultValue: Boolean): Boolean
    fun getInt(key: String?): Int
    fun getInt(key: String?, defaultValue: Int): Int
    fun getLong(key: String?): Long
    fun getLong(key: String?, defaultValue: Long): Long
    fun getDouble(key: String?): Double
    fun getDouble(key: String?, defaultValue: Double): Double
    fun getString(key: String?): String?
    fun getString(key: String?, defaultValue: String?): String?
    fun getBooleanArray(key: String?): BooleanArray?
    fun getIntArray(key: String?): IntArray?
    fun getLongArray(key: String?): LongArray?
    fun getDoubleArray(key: String?): DoubleArray?
    fun getStringArray(key: String?): Array<String?>?
}

/**
 * A mapping from String keys to values of various types that can be saved to persistent and later
 * restored.
 */
expect final class PersistableBundle : BaseBundle, Cloneable, Parcelable {
    constructor()
    constructor(capacity: Int)
    constructor(b: PersistableBundle?)

    fun putPersistableBundle(key: String?, value: PersistableBundle?)
    fun getPersistableBundle(key: String?): PersistableBundle?
    fun clone(): Any
    override fun describeContents(): Int
    override fun writeToParcel(dest: Parcel, flags: Int)
    override fun toString(): String
}

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
    }
}
