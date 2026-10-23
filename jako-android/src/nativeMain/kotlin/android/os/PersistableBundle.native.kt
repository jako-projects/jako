package android.os

import android.content.platformAppCacheDir
import android.content.platformAppDataDir
import android.content.platformDefaultExternalStorageDir
import java.lang.Cloneable
import kotlinx.cinterop.sizeOf
import platform.posix.getpid

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

internal const val VAL_NULL = -1

internal const val VAL_STRING = 0

internal const val VAL_INTEGER = 1

internal const val VAL_LONG = 2

internal const val VAL_FLOAT = 3

internal const val VAL_DOUBLE = 4

internal const val VAL_BOOLEAN = 5

internal const val VAL_BYTE = 6

internal const val VAL_SHORT = 7

internal const val VAL_CHAR = 8

internal const val VAL_BYTEARRAY = 9

internal const val VAL_INTARRAY = 10

internal const val VAL_LONGARRAY = 11

internal const val VAL_STRINGARRAY = 12

internal const val VAL_BUNDLE = 13

internal fun writeValueToParcel(dest: Parcel, v: Any?) {
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
