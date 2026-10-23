package android.os

import android.content.platformAppCacheDir
import android.content.platformAppDataDir
import android.content.platformDefaultExternalStorageDir
import kotlinx.cinterop.sizeOf
import platform.posix.getpid

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

    actual fun writeBooleanArray(valArray: BooleanArray?) {
        if (valArray == null) {
            writeInt(-1)
        } else {
            writeInt(valArray.size)
            for (v in valArray) writeInt(if (v) 1 else 0)
        }
    }

    actual fun createBooleanArray(): BooleanArray? {
        val n = readInt()
        if (n < 0) return null
        return BooleanArray(n) { readInt() != 0 }
    }

    actual fun writeCharArray(valArray: CharArray?) {
        if (valArray == null) {
            writeInt(-1)
        } else {
            writeInt(valArray.size)
            for (v in valArray) writeInt(v.code)
        }
    }

    actual fun createCharArray(): CharArray? {
        val n = readInt()
        if (n < 0) return null
        return CharArray(n) { readInt().toChar() }
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

    actual fun writeFloatArray(valArray: FloatArray?) {
        if (valArray == null) {
            writeInt(-1)
        } else {
            writeInt(valArray.size)
            for (v in valArray) writeFloat(v)
        }
    }

    actual fun createFloatArray(): FloatArray? {
        val n = readInt()
        if (n < 0) return null
        return FloatArray(n) { readFloat() }
    }

    actual fun writeDoubleArray(valArray: DoubleArray?) {
        if (valArray == null) {
            writeInt(-1)
        } else {
            writeInt(valArray.size)
            for (v in valArray) writeDouble(v)
        }
    }

    actual fun createDoubleArray(): DoubleArray? {
        val n = readInt()
        if (n < 0) return null
        return DoubleArray(n) { readDouble() }
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

    actual fun writeStringList(valList: List<String>?) {
        if (valList == null) {
            writeInt(-1)
        } else {
            writeInt(valList.size)
            for (s in valList) writeString(s)
        }
    }

    actual fun createStringArrayList(): ArrayList<String>? {
        val n = readInt()
        if (n < 0) return null
        val list = ArrayList<String>(n)
        for (i in 0 until n) {
            list.add(readString() ?: "")
        }
        return list
    }

    actual fun <T : Parcelable?> writeTypedObject(valObj: T, parcelableFlags: Int) {
        if (valObj != null) {
            writeInt(1)
            valObj.writeToParcel(this, parcelableFlags)
        } else {
            writeInt(0)
        }
    }

    actual fun <T> readTypedObject(c: Parcelable.Creator<T>): T? {
        return if (readInt() != 0) {
            c.createFromParcel(this)
        } else {
            null
        }
    }

    actual fun <T : Parcelable?> writeTypedList(valList: List<T>?) {
        if (valList == null) {
            writeInt(-1)
            return
        }
        writeInt(valList.size)
        for (item in valList) {
            writeTypedObject(item, 0)
        }
    }

    actual fun <T> createTypedArrayList(c: Parcelable.Creator<T>): ArrayList<T>? {
        val n = readInt()
        if (n < 0) return null
        val list = ArrayList<T>(n)
        for (i in 0 until n) {
            if (readInt() != 0) {
                list.add(c.createFromParcel(this))
            }
        }
        return list
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
