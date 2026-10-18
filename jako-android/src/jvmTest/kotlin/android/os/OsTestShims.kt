package android.os

class SystemProperties {
    companion object {
        @JvmStatic
        fun get(key: String): String = ""

        @JvmStatic
        fun get(key: String, def: String?): String? = def

        @JvmStatic
        fun getInt(key: String, def: Int): Int = def

        @JvmStatic
        fun getLong(key: String, def: Long): Long = def

        @JvmStatic
        fun getBoolean(key: String, def: Boolean): Boolean = def
    }
}

/**
 * JVM test-only shims for `SystemClock`, `Process`, and `Parcel` which invoke C++ JNI methods
 * in `org.robolectric:android-all` when executed outside an Android Runtime / Robolectric runner.
 * Note: `Build`, `BaseBundle`, `Bundle`, `PersistableBundle`, and `Parcelable` run directly against
 * real AOSP bytecode from `android-all`.
 */
final class SystemClock private constructor() {
    companion object {
        @JvmStatic
        fun sleep(ms: Long) {
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

        @JvmStatic
        fun uptimeMillis(): Long = System.nanoTime() / 1_000_000L

        @JvmStatic
        fun elapsedRealtime(): Long = System.nanoTime() / 1_000_000L

        @JvmStatic
        fun elapsedRealtimeNanos(): Long = System.nanoTime()
    }
}

open class Process {
    companion object {
        const val THREAD_PRIORITY_DEFAULT: Int = 0
        const val THREAD_PRIORITY_LOWEST: Int = 19
        const val THREAD_PRIORITY_BACKGROUND: Int = 10
        const val THREAD_PRIORITY_FOREGROUND: Int = -2
        const val THREAD_PRIORITY_DISPLAY: Int = -4
        const val THREAD_PRIORITY_URGENT_DISPLAY: Int = -8
        const val THREAD_PRIORITY_VIDEO: Int = -10
        const val THREAD_PRIORITY_AUDIO: Int = -16
        const val THREAD_PRIORITY_URGENT_AUDIO: Int = -19
        const val THREAD_PRIORITY_MORE_FAVORABLE: Int = -1
        const val THREAD_PRIORITY_LESS_FAVORABLE: Int = 1

        @JvmStatic
        fun myPid(): Int = ProcessHandle.current().pid().toInt()

        @JvmStatic
        fun myTid(): Int = Thread.currentThread().id.toInt()

        @JvmStatic
        fun myUid(): Int = 0

        @JvmStatic
        fun is64Bit(): Boolean = System.getProperty("sun.arch.data.model") == "64" ||
            System.getProperty("os.arch")?.contains("64") == true
    }
}

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
