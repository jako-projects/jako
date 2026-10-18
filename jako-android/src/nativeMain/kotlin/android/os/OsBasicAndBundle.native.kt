package android.os

import android.util.ArrayMap
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.Serializable
import java.lang.Cloneable
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.sizeOf
import platform.posix.getpid

actual final class SystemClock private constructor() {
    actual companion object {
        actual fun sleep(ms: Long) {
            var start = uptimeMillis()
            var duration = ms
            var interrupted = false
            do {
                try {
                    Thread.sleep(duration)
                } catch (_: InterruptedException) {
                    interrupted = true
                }
                duration = start + ms - uptimeMillis()
            } while (duration > 0)
            if (interrupted) {
                Thread.currentThread().interrupt()
            }
        }

        actual fun uptimeMillis(): Long = System.nanoTime() / 1_000_000L

        actual fun elapsedRealtime(): Long = System.nanoTime() / 1_000_000L

        actual fun elapsedRealtimeNanos(): Long = System.nanoTime()
    }
}

actual open class Build actual constructor() {
    actual class VERSION actual constructor() {
        actual companion object {
            actual val INCREMENTAL: String = "unknown"
            actual val RELEASE: String = "10"
            actual val SDK: String = "29"
            actual val SDK_INT: Int = 29
            actual val CODENAME: String = "REL"
        }
    }

    actual class VERSION_CODES actual constructor() {
        actual companion object {
            actual val CUR_DEVELOPMENT: Int = 10000
            actual val BASE: Int = 1
            actual val BASE_1_1: Int = 2
            actual val CUPCAKE: Int = 3
            actual val DONUT: Int = 4
            actual val ECLAIR: Int = 5
            actual val ECLAIR_0_1: Int = 6
            actual val ECLAIR_MR1: Int = 7
            actual val FROYO: Int = 8
            actual val GINGERBREAD: Int = 9
            actual val GINGERBREAD_MR1: Int = 10
            actual val HONEYCOMB: Int = 11
            actual val HONEYCOMB_MR1: Int = 12
            actual val HONEYCOMB_MR2: Int = 13
            actual val ICE_CREAM_SANDWICH: Int = 14
            actual val ICE_CREAM_SANDWICH_MR1: Int = 15
            actual val JELLY_BEAN: Int = 16
            actual val JELLY_BEAN_MR1: Int = 17
            actual val JELLY_BEAN_MR2: Int = 18
            actual val KITKAT: Int = 19
            actual val KITKAT_WATCH: Int = 20
            actual val LOLLIPOP: Int = 21
            actual val LOLLIPOP_MR1: Int = 22
            actual val M: Int = 23
            actual val N: Int = 24
            actual val N_MR1: Int = 25
            actual val O: Int = 26
            actual val O_MR1: Int = 27
            actual val P: Int = 28
            actual val Q: Int = 29
        }
    }

    actual companion object {
        actual val UNKNOWN: String = "unknown"
        actual val ID: String = UNKNOWN
        actual val DISPLAY: String = UNKNOWN
        actual val PRODUCT: String = UNKNOWN
        actual val DEVICE: String = UNKNOWN
        actual val BOARD: String = UNKNOWN
        actual val MANUFACTURER: String = UNKNOWN
        actual val BRAND: String = UNKNOWN
        actual val MODEL: String = UNKNOWN
        actual val BOOTLOADER: String = UNKNOWN
        actual val HARDWARE: String = UNKNOWN
        actual val TYPE: String = "user"
        actual val TAGS: String = UNKNOWN
        actual val FINGERPRINT: String = UNKNOWN
        actual val USER: String = UNKNOWN
        actual val HOST: String = UNKNOWN
    }
}

actual open class Process actual constructor() {
    actual companion object {
        actual val THREAD_PRIORITY_DEFAULT: Int = 0
        actual val THREAD_PRIORITY_LOWEST: Int = 19
        actual val THREAD_PRIORITY_BACKGROUND: Int = 10
        actual val THREAD_PRIORITY_FOREGROUND: Int = -2
        actual val THREAD_PRIORITY_DISPLAY: Int = -4
        actual val THREAD_PRIORITY_URGENT_DISPLAY: Int = -8
        actual val THREAD_PRIORITY_VIDEO: Int = -10
        actual val THREAD_PRIORITY_AUDIO: Int = -16
        actual val THREAD_PRIORITY_URGENT_AUDIO: Int = -19
        actual val THREAD_PRIORITY_MORE_FAVORABLE: Int = -1
        actual val THREAD_PRIORITY_LESS_FAVORABLE: Int = 1

        actual fun myPid(): Int = getpid()
        actual fun myTid(): Int = Thread.currentThread().id.toInt()
        actual fun myUid(): Int = 0
        actual fun is64Bit(): Boolean = sizeOf<COpaquePointerVar>() == 8L
    }
}

actual interface Parcelable {
    actual fun describeContents(): Int
    actual fun writeToParcel(dest: Parcel, flags: Int)

    actual interface Creator<T> {
        actual fun createFromParcel(source: Parcel): T
        actual fun newArray(size: Int): Array<T?>
    }

    actual companion object {
        actual val PARCELABLE_WRITE_RETURN_VALUE: Int = 1
        actual val CONTENTS_FILE_DESCRIPTOR: Int = 1
    }
}

actual final class Parcel private constructor() {
    private var buffer = ByteArray(64)
    private var pos = 0
    private var size = 0

    private fun ensureCapacity(needed: Int) {
        val required = pos + needed
        if (required > buffer.size) {
            var newCap = buffer.size * 2
            while (newCap < required) newCap *= 2
            buffer = buffer.copyOf(newCap)
        }
    }

    actual fun recycle() {
        pos = 0
        size = 0
    }

    actual fun dataPosition(): Int = pos

    actual fun setDataPosition(pos: Int) {
        require(pos in 0..size) { "Invalid dataPosition: $pos (size=$size)" }
        this.pos = pos
    }

    actual fun dataSize(): Int = size

    actual fun writeByte(valByte: Byte) {
        ensureCapacity(1)
        buffer[pos++] = valByte
        if (pos > size) size = pos
    }

    actual fun readByte(): Byte {
        if (pos >= size) return 0
        return buffer[pos++]
    }

    actual fun writeInt(valInt: Int) {
        ensureCapacity(4)
        buffer[pos++] = (valInt ushr 24).toByte()
        buffer[pos++] = (valInt ushr 16).toByte()
        buffer[pos++] = (valInt ushr 8).toByte()
        buffer[pos++] = valInt.toByte()
        if (pos > size) size = pos
    }

    actual fun readInt(): Int {
        if (pos + 4 > size) return 0
        val b0 = (buffer[pos++].toInt() and 0xFF) shl 24
        val b1 = (buffer[pos++].toInt() and 0xFF) shl 16
        val b2 = (buffer[pos++].toInt() and 0xFF) shl 8
        val b3 = buffer[pos++].toInt() and 0xFF
        return b0 or b1 or b2 or b3
    }

    actual fun writeLong(valLong: Long) {
        writeInt((valLong ushr 32).toInt())
        writeInt(valLong.toInt())
    }

    actual fun readLong(): Long {
        val hi = readInt().toLong() and 0xFFFFFFFFL
        val lo = readInt().toLong() and 0xFFFFFFFFL
        return (hi shl 32) or lo
    }

    actual fun writeFloat(valFloat: Float) {
        writeInt(valFloat.toRawBits())
    }

    actual fun readFloat(): Float {
        return Float.fromBits(readInt())
    }

    actual fun writeDouble(valDouble: Double) {
        writeLong(valDouble.toRawBits())
    }

    actual fun readDouble(): Double {
        return Double.fromBits(readLong())
    }

    actual fun writeString(valStr: String?) {
        if (valStr == null) {
            writeInt(-1)
        } else {
            val bytes = valStr.encodeToByteArray()
            writeInt(bytes.size)
            ensureCapacity(bytes.size)
            bytes.copyInto(buffer, pos, 0, bytes.size)
            pos += bytes.size
            if (pos > size) size = pos
        }
    }

    actual fun readString(): String? {
        val len = readInt()
        if (len < 0) return null
        if (len == 0) return ""
        if (pos + len > size) return null
        val str = buffer.decodeToString(pos, pos + len)
        pos += len
        return str
    }

    actual fun writeByteArray(b: ByteArray?) {
        if (b == null) {
            writeInt(-1)
        } else {
            writeInt(b.size)
            ensureCapacity(b.size)
            b.copyInto(buffer, pos, 0, b.size)
            pos += b.size
            if (pos > size) size = pos
        }
    }

    actual fun createByteArray(): ByteArray? {
        val len = readInt()
        if (len < 0) return null
        if (pos + len > size) return null
        val out = buffer.copyOfRange(pos, pos + len)
        pos += len
        return out
    }

    actual fun writeIntArray(valArray: IntArray?) {
        if (valArray == null) {
            writeInt(-1)
        } else {
            writeInt(valArray.size)
            for (v in valArray) writeInt(v)
        }
    }

    actual fun createIntArray(): IntArray? {
        val n = readInt()
        if (n < 0) return null
        return IntArray(n) { readInt() }
    }

    actual fun writeLongArray(valArray: LongArray?) {
        if (valArray == null) {
            writeInt(-1)
        } else {
            writeInt(valArray.size)
            for (v in valArray) writeLong(v)
        }
    }

    actual fun createLongArray(): LongArray? {
        val n = readInt()
        if (n < 0) return null
        return LongArray(n) { readLong() }
    }

    actual fun writeStringArray(valArray: Array<String?>?) {
        if (valArray == null) {
            writeInt(-1)
        } else {
            writeInt(valArray.size)
            for (s in valArray) writeString(s)
        }
    }

    actual fun createStringArray(): Array<String?>? {
        val n = readInt()
        if (n < 0) return null
        return Array(n) { readString() }
    }

    actual fun writeBundle(valBundle: Bundle?) {
        if (valBundle == null) {
            writeInt(-1)
        } else {
            writeInt(1)
            valBundle.writeToParcel(this, 0)
        }
    }

    actual fun readBundle(): Bundle? {
        val flag = readInt()
        if (flag < 0) return null
        val b = Bundle()
        b.readFromParcel(this)
        return b
    }

    actual fun marshall(): ByteArray = buffer.copyOfRange(0, size)

    actual fun unmarshall(data: ByteArray, offset: Int, length: Int) {
        require(offset >= 0 && length >= 0 && offset + length <= data.size)
        buffer = data.copyOfRange(offset, offset + length)
        pos = 0
        size = length
    }

    actual companion object {
        actual fun obtain(): Parcel = Parcel()
    }
}

@Suppress("UNCHECKED_CAST")
actual open class BaseBundle {
    internal val map: ArrayMap<String?, Any?>

    constructor() {
        map = ArrayMap()
    }

    constructor(capacity: Int) {
        map = if (capacity > 0) ArrayMap(capacity) else ArrayMap()
    }

    constructor(b: BaseBundle?) {
        map = ArrayMap()
        if (b != null) {
            map.putAll(b.map)
        }
    }

    actual val isEmpty: Boolean
        get() = map.isEmpty()

    actual fun size(): Int = map.size

    actual fun isEmpty(): Boolean = map.isEmpty()

    actual fun clear() = map.clear()

    actual fun containsKey(key: String?): Boolean = map.containsKey(key)

    actual operator fun get(key: String?): Any? = map[key]

    actual fun remove(key: String?) {
        map.remove(key)
    }

    actual fun putAll(bundle: PersistableBundle) {
        map.putAll(bundle.map)
    }

    actual fun keySet(): MutableSet<String> = map.keys as MutableSet<String>

    actual fun putBoolean(key: String?, value: Boolean) {
        map[key] = value
    }

    actual fun putInt(key: String?, value: Int) {
        map[key] = value
    }

    actual fun putLong(key: String?, value: Long) {
        map[key] = value
    }

    actual fun putDouble(key: String?, value: Double) {
        map[key] = value
    }

    actual fun putString(key: String?, value: String?) {
        map[key] = value
    }

    actual fun putBooleanArray(key: String?, value: BooleanArray?) {
        map[key] = value
    }

    actual fun putIntArray(key: String?, value: IntArray?) {
        map[key] = value
    }

    actual fun putLongArray(key: String?, value: LongArray?) {
        map[key] = value
    }

    actual fun putDoubleArray(key: String?, value: DoubleArray?) {
        map[key] = value
    }

    actual fun putStringArray(key: String?, value: Array<String?>?) {
        map[key] = value
    }

    actual fun getBoolean(key: String?): Boolean = getBoolean(key, false)

    actual fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        return (map[key] as? Boolean) ?: defaultValue
    }

    actual fun getInt(key: String?): Int = getInt(key, 0)

    actual fun getInt(key: String?, defaultValue: Int): Int {
        return (map[key] as? Int) ?: defaultValue
    }

    actual fun getLong(key: String?): Long = getLong(key, 0L)

    actual fun getLong(key: String?, defaultValue: Long): Long {
        return (map[key] as? Long) ?: defaultValue
    }

    actual fun getDouble(key: String?): Double = getDouble(key, 0.0)

    actual fun getDouble(key: String?, defaultValue: Double): Double {
        return (map[key] as? Double) ?: defaultValue
    }

    actual fun getString(key: String?): String? = map[key] as? String

    actual fun getString(key: String?, defaultValue: String?): String? {
        return (map[key] as? String) ?: defaultValue
    }

    actual fun getBooleanArray(key: String?): BooleanArray? = map[key] as? BooleanArray

    actual fun getIntArray(key: String?): IntArray? = map[key] as? IntArray

    actual fun getLongArray(key: String?): LongArray? = map[key] as? LongArray

    actual fun getDoubleArray(key: String?): DoubleArray? = map[key] as? DoubleArray

    actual fun getStringArray(key: String?): Array<String?>? = map[key] as? Array<String?>
}

actual final class PersistableBundle : BaseBundle, Cloneable, Parcelable {
    actual constructor() : super()
    actual constructor(capacity: Int) : super(capacity)
    actual constructor(b: PersistableBundle?) : super(b)

    actual fun putPersistableBundle(key: String?, value: PersistableBundle?) {
        map[key] = value
    }

    actual fun getPersistableBundle(key: String?): PersistableBundle? {
        return map[key] as? PersistableBundle
    }

    actual fun clone(): Any = PersistableBundle(this)

    actual override fun describeContents(): Int = 0

    actual override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(map.size)
        for ((k, v) in map) {
            dest.writeString(k)
            writeValueToParcel(dest, v)
        }
    }

    actual override fun toString(): String = "PersistableBundle[$map]"
}

@Suppress("UNCHECKED_CAST")
actual final class Bundle : BaseBundle, Cloneable, Parcelable {
    actual constructor() : super()
    actual constructor(capacity: Int) : super(capacity)
    actual constructor(b: Bundle?) : super(b)
    actual constructor(b: PersistableBundle?) : super(b)

    actual fun clone(): Any = Bundle(this)

    actual fun deepCopy(): Bundle {
        val b = Bundle(map.size)
        for (i in 0 until map.size) {
            val k = map.keyAt(i)
            val v = when (val raw = map.valueAt(i)) {
                is Bundle -> raw.deepCopy()
                is PersistableBundle -> PersistableBundle(raw)
                is ByteArray -> raw.copyOf()
                is ShortArray -> raw.copyOf()
                is IntArray -> raw.copyOf()
                is LongArray -> raw.copyOf()
                is FloatArray -> raw.copyOf()
                is DoubleArray -> raw.copyOf()
                is BooleanArray -> raw.copyOf()
                is CharArray -> raw.copyOf()
                is Array<*> -> raw.copyOf()
                is ArrayList<*> -> ArrayList(raw)
                else -> raw
            }
            b.map[k] = v
        }
        return b
    }

    actual fun putAll(bundle: Bundle) {
        map.putAll(bundle.map)
    }

    actual fun hasFileDescriptors(): Boolean = false

    actual fun putByte(key: String?, value: Byte) {
        map[key] = value
    }

    actual fun putChar(key: String?, value: Char) {
        map[key] = value
    }

    actual fun putShort(key: String?, value: Short) {
        map[key] = value
    }

    actual fun putFloat(key: String?, value: Float) {
        map[key] = value
    }

    actual fun putCharSequence(key: String?, value: CharSequence?) {
        map[key] = value
    }

    actual fun putBundle(key: String?, value: Bundle?) {
        map[key] = value
    }

    actual fun putParcelable(key: String?, value: Parcelable?) {
        map[key] = value
    }

    actual fun putSerializable(key: String?, value: Serializable?) {
        map[key] = value
    }

    actual fun putByteArray(key: String?, value: ByteArray?) {
        map[key] = value
    }

    actual fun putShortArray(key: String?, value: ShortArray?) {
        map[key] = value
    }

    actual fun putCharArray(key: String?, value: CharArray?) {
        map[key] = value
    }

    actual fun putFloatArray(key: String?, value: FloatArray?) {
        map[key] = value
    }

    actual fun putCharSequenceArray(key: String?, value: Array<CharSequence?>?) {
        map[key] = value
    }

    actual fun putIntegerArrayList(key: String?, value: ArrayList<Int>?) {
        map[key] = value
    }

    actual fun putStringArrayList(key: String?, value: ArrayList<String>?) {
        map[key] = value
    }

    actual fun putCharSequenceArrayList(key: String?, value: ArrayList<CharSequence>?) {
        map[key] = value
    }

    actual fun getByte(key: String?): Byte = getByte(key, 0)

    actual fun getByte(key: String?, defaultValue: Byte): Byte {
        return (map[key] as? Byte) ?: defaultValue
    }

    actual fun getChar(key: String?): Char = getChar(key, 0.toChar())

    actual fun getChar(key: String?, defaultValue: Char): Char {
        return (map[key] as? Char) ?: defaultValue
    }

    actual fun getShort(key: String?): Short = getShort(key, 0)

    actual fun getShort(key: String?, defaultValue: Short): Short {
        return (map[key] as? Short) ?: defaultValue
    }

    actual fun getFloat(key: String?): Float = getFloat(key, 0f)

    actual fun getFloat(key: String?, defaultValue: Float): Float {
        return (map[key] as? Float) ?: defaultValue
    }

    actual fun getCharSequence(key: String?): CharSequence? = map[key] as? CharSequence

    actual fun getCharSequence(key: String?, defaultValue: CharSequence?): CharSequence? {
        return (map[key] as? CharSequence) ?: defaultValue
    }

    actual fun getBundle(key: String?): Bundle? = map[key] as? Bundle

    actual fun <T : Parcelable> getParcelable(key: String?): T? = map[key] as? T

    actual fun getSerializable(key: String?): Serializable? = map[key] as? Serializable

    actual fun getByteArray(key: String?): ByteArray? = map[key] as? ByteArray

    actual fun getShortArray(key: String?): ShortArray? = map[key] as? ShortArray

    actual fun getCharArray(key: String?): CharArray? = map[key] as? CharArray

    actual fun getFloatArray(key: String?): FloatArray? = map[key] as? FloatArray

    actual fun getCharSequenceArray(key: String?): Array<CharSequence?>? = map[key] as? Array<CharSequence?>

    actual fun getIntegerArrayList(key: String?): ArrayList<Int>? = map[key] as? ArrayList<Int>

    actual fun getStringArrayList(key: String?): ArrayList<String>? = map[key] as? ArrayList<String>

    actual fun getCharSequenceArrayList(key: String?): ArrayList<CharSequence>? = map[key] as? ArrayList<CharSequence>

    actual override fun describeContents(): Int = 0

    actual override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(map.size)
        for ((k, v) in map) {
            dest.writeString(k)
            writeValueToParcel(dest, v)
        }
    }

    internal fun readFromParcel(source: Parcel) {
        val count = source.readInt()
        for (i in 0 until count) {
            val k = source.readString()
            val v = readValueFromParcel(source)
            map[k] = v
        }
    }

    actual override fun toString(): String = "Bundle[$map]"

    actual companion object {
        actual val EMPTY: Bundle = Bundle()
    }
}

private const val VAL_NULL = -1
private const val VAL_STRING = 0
private const val VAL_INTEGER = 1
private const val VAL_LONG = 2
private const val VAL_FLOAT = 3
private const val VAL_DOUBLE = 4
private const val VAL_BOOLEAN = 5
private const val VAL_BYTE = 6
private const val VAL_SHORT = 7
private const val VAL_CHAR = 8
private const val VAL_BYTEARRAY = 9
private const val VAL_INTARRAY = 10
private const val VAL_LONGARRAY = 11
private const val VAL_STRINGARRAY = 12
private const val VAL_BUNDLE = 13

private fun writeValueToParcel(dest: Parcel, v: Any?) {
    when (v) {
        null -> dest.writeInt(VAL_NULL)
        is String -> {
            dest.writeInt(VAL_STRING)
            dest.writeString(v)
        }
        is Int -> {
            dest.writeInt(VAL_INTEGER)
            dest.writeInt(v)
        }
        is Long -> {
            dest.writeInt(VAL_LONG)
            dest.writeLong(v)
        }
        is Float -> {
            dest.writeInt(VAL_FLOAT)
            dest.writeFloat(v)
        }
        is Double -> {
            dest.writeInt(VAL_DOUBLE)
            dest.writeDouble(v)
        }
        is Boolean -> {
            dest.writeInt(VAL_BOOLEAN)
            dest.writeInt(if (v) 1 else 0)
        }
        is Byte -> {
            dest.writeInt(VAL_BYTE)
            dest.writeByte(v)
        }
        is Short -> {
            dest.writeInt(VAL_SHORT)
            dest.writeInt(v.toInt())
        }
        is Char -> {
            dest.writeInt(VAL_CHAR)
            dest.writeInt(v.code)
        }
        is ByteArray -> {
            dest.writeInt(VAL_BYTEARRAY)
            dest.writeByteArray(v)
        }
        is IntArray -> {
            dest.writeInt(VAL_INTARRAY)
            dest.writeIntArray(v)
        }
        is LongArray -> {
            dest.writeInt(VAL_LONGARRAY)
            dest.writeLongArray(v)
        }
        is Array<*> -> {
            dest.writeInt(VAL_STRINGARRAY)
            @Suppress("UNCHECKED_CAST")
            dest.writeStringArray(v as Array<String?>)
        }
        is Bundle -> {
            dest.writeInt(VAL_BUNDLE)
            dest.writeBundle(v)
        }
        else -> {
            dest.writeInt(VAL_STRING)
            dest.writeString(v.toString())
        }
    }
}

private fun readValueFromParcel(source: Parcel): Any? {
    return when (source.readInt()) {
        VAL_NULL -> null
        VAL_STRING -> source.readString()
        VAL_INTEGER -> source.readInt()
        VAL_LONG -> source.readLong()
        VAL_FLOAT -> source.readFloat()
        VAL_DOUBLE -> source.readDouble()
        VAL_BOOLEAN -> source.readInt() != 0
        VAL_BYTE -> source.readByte()
        VAL_SHORT -> source.readInt().toShort()
        VAL_CHAR -> source.readInt().toChar()
        VAL_BYTEARRAY -> source.createByteArray()
        VAL_INTARRAY -> source.createIntArray()
        VAL_LONGARRAY -> source.createLongArray()
        VAL_STRINGARRAY -> source.createStringArray()
        VAL_BUNDLE -> source.readBundle()
        else -> null
    }
}
