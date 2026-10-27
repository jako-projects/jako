package android.os

import android.content.platformAppCacheDir
import android.content.platformAppDataDir
import android.content.platformDefaultExternalStorageDir
import java.io.Serializable
import java.lang.Cloneable

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
        actual val CREATOR: Parcelable.Creator<Bundle> = object : Parcelable.Creator<Bundle> {
            override fun createFromParcel(source: Parcel): Bundle {
                val b = Bundle()
                b.readFromParcel(source)
                return b
            }

            override fun newArray(size: Int): Array<Bundle?> = arrayOfNulls(size)
        }
    }
}

internal fun readValueFromParcel(source: Parcel): Any? {
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
