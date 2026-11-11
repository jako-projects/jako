package android.os

final class Parcel private constructor() {
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

    fun recycle() {
        pos = 0
        size = 0
    }

    fun dataPosition(): Int = pos

    fun setDataPosition(pos: Int) {
        require(pos in 0..size)
        this.pos = pos
    }

    fun dataSize(): Int = size

    fun writeByte(valByte: Byte) {
        ensureCapacity(1)
        buffer[pos++] = valByte
        if (pos > size) size = pos
    }

    fun readByte(): Byte = if (pos >= size) 0 else buffer[pos++]

    fun writeInt(valInt: Int) {
        ensureCapacity(4)
        buffer[pos++] = (valInt ushr 24).toByte()
        buffer[pos++] = (valInt ushr 16).toByte()
        buffer[pos++] = (valInt ushr 8).toByte()
        buffer[pos++] = valInt.toByte()
        if (pos > size) size = pos
    }

    fun readInt(): Int {
        if (pos + 4 > size) return 0
        val b0 = (buffer[pos++].toInt() and 0xFF) shl 24
        val b1 = (buffer[pos++].toInt() and 0xFF) shl 16
        val b2 = (buffer[pos++].toInt() and 0xFF) shl 8
        val b3 = buffer[pos++].toInt() and 0xFF
        return b0 or b1 or b2 or b3
    }

    fun writeLong(valLong: Long) {
        writeInt((valLong ushr 32).toInt())
        writeInt(valLong.toInt())
    }

    fun readLong(): Long {
        val hi = readInt().toLong() and 0xFFFFFFFFL
        val lo = readInt().toLong() and 0xFFFFFFFFL
        return (hi shl 32) or lo
    }

    fun writeFloat(valFloat: Float) = writeInt(valFloat.toRawBits())
    fun readFloat(): Float = Float.fromBits(readInt())
    fun writeDouble(valDouble: Double) = writeLong(valDouble.toRawBits())
    fun readDouble(): Double = Double.fromBits(readLong())

    fun writeString(valStr: String?) {
        if (valStr == null) {
            writeInt(-1)
        } else {
            val bytes = valStr.toByteArray(Charsets.UTF_8)
            writeInt(bytes.size)
            ensureCapacity(bytes.size)
            bytes.copyInto(buffer, pos, 0, bytes.size)
            pos += bytes.size
            if (pos > size) size = pos
        }
    }

    fun readString(): String? {
        val len = readInt()
        if (len < 0) return null
        if (len == 0) return ""
        if (pos + len > size) return null
        val str = String(buffer, pos, len, Charsets.UTF_8)
        pos += len
        return str
    }

    fun writeByteArray(b: ByteArray?) {
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

    fun createByteArray(): ByteArray? {
        val len = readInt()
        if (len < 0) return null
        if (pos + len > size) return null
        val out = buffer.copyOfRange(pos, pos + len)
        pos += len
        return out
    }

    fun writeBooleanArray(valArray: BooleanArray?) {
        if (valArray == null) writeInt(-1)
        else {
            writeInt(valArray.size)
            for (v in valArray) writeInt(if (v) 1 else 0)
        }
    }

    fun createBooleanArray(): BooleanArray? {
        val n = readInt()
        if (n < 0) return null
        return BooleanArray(n) { readInt() != 0 }
    }

    fun writeCharArray(valArray: CharArray?) {
        if (valArray == null) writeInt(-1)
        else {
            writeInt(valArray.size)
            for (v in valArray) writeInt(v.code)
        }
    }

    fun createCharArray(): CharArray? {
        val n = readInt()
        if (n < 0) return null
        return CharArray(n) { readInt().toChar() }
    }

    fun writeIntArray(valArray: IntArray?) {
        if (valArray == null) writeInt(-1)
        else {
            writeInt(valArray.size)
            for (v in valArray) writeInt(v)
        }
    }

    fun createIntArray(): IntArray? {
        val n = readInt()
        if (n < 0) return null
        return IntArray(n) { readInt() }
    }

    fun writeLongArray(valArray: LongArray?) {
        if (valArray == null) writeInt(-1)
        else {
            writeInt(valArray.size)
            for (v in valArray) writeLong(v)
        }
    }

    fun createLongArray(): LongArray? {
        val n = readInt()
        if (n < 0) return null
        return LongArray(n) { readLong() }
    }

    fun writeFloatArray(valArray: FloatArray?) {
        if (valArray == null) writeInt(-1)
        else {
            writeInt(valArray.size)
            for (v in valArray) writeFloat(v)
        }
    }

    fun createFloatArray(): FloatArray? {
        val n = readInt()
        if (n < 0) return null
        return FloatArray(n) { readFloat() }
    }

    fun writeDoubleArray(valArray: DoubleArray?) {
        if (valArray == null) writeInt(-1)
        else {
            writeInt(valArray.size)
            for (v in valArray) writeDouble(v)
        }
    }

    fun createDoubleArray(): DoubleArray? {
        val n = readInt()
        if (n < 0) return null
        return DoubleArray(n) { readDouble() }
    }

    fun writeStringArray(valArray: Array<String?>?) {
        if (valArray == null) writeInt(-1)
        else {
            writeInt(valArray.size)
            for (s in valArray) writeString(s)
        }
    }

    fun createStringArray(): Array<String?>? {
        val n = readInt()
        if (n < 0) return null
        return Array(n) { readString() }
    }

    fun writeStringList(valList: List<String>?) {
        if (valList == null) writeInt(-1)
        else {
            writeInt(valList.size)
            for (s in valList) writeString(s)
        }
    }

    fun createStringArrayList(): ArrayList<String>? {
        val n = readInt()
        if (n < 0) return null
        val list = ArrayList<String>(n)
        for (i in 0 until n) {
            list.add(readString() ?: "")
        }
        return list
    }

    fun <T : Parcelable?> writeTypedObject(valObj: T, parcelableFlags: Int) {
        if (valObj != null) {
            writeInt(1)
            valObj.writeToParcel(this, parcelableFlags)
        } else {
            writeInt(0)
        }
    }

    fun <T> readTypedObject(c: Parcelable.Creator<T>): T? {
        return if (readInt() != 0) {
            c.createFromParcel(this)
        } else {
            null
        }
    }

    fun <T : Parcelable?> writeTypedList(valList: List<T>?) {
        if (valList == null) {
            writeInt(-1)
            return
        }
        writeInt(valList.size)
        for (item in valList) {
            writeTypedObject(item, 0)
        }
    }

    fun <T> createTypedArrayList(c: Parcelable.Creator<T>): ArrayList<T>? {
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

    fun writeBundle(valBundle: Bundle?) {
        if (valBundle == null) {
            writeInt(-1)
        } else {
            writeInt(valBundle.size())
            for (k in valBundle.keySet()) {
                writeString(k)
                when (val v = valBundle.get(k)) {
                    null -> writeInt(-1)
                    is String -> { writeInt(0); writeString(v) }
                    is Int -> { writeInt(1); writeInt(v) }
                    is Long -> { writeInt(2); writeLong(v) }
                    is Boolean -> { writeInt(5); writeInt(if (v) 1 else 0) }
                    else -> { writeInt(0); writeString(v.toString()) }
                }
            }
        }
    }

    fun readBundle(): Bundle? {
        val count = readInt()
        if (count < 0) return null
        val b = Bundle()
        for (i in 0 until count) {
            val k = readString()
            when (readInt()) {
                0 -> b.putString(k, readString())
                1 -> b.putInt(k, readInt())
                2 -> b.putLong(k, readLong())
                5 -> b.putBoolean(k, readInt() != 0)
            }
        }
        return b
    }

    fun marshall(): ByteArray = buffer.copyOfRange(0, size)

    fun unmarshall(data: ByteArray, offset: Int, length: Int) {
        buffer = data.copyOfRange(offset, offset + length)
        pos = 0
        size = length
    }

    companion object {
        @JvmStatic
        fun obtain(): Parcel = Parcel()
    }
}
