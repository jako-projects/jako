package java.io

actual open class ObjectOutputStream : OutputStream, ObjectOutput, ObjectStreamConstants {
    private val dataOut: DataOutputStream?

    actual constructor(out: OutputStream) {
        val dout = DataOutputStream(out)
        dout.writeShort(STREAM_MAGIC_INT)
        dout.writeShort(STREAM_VERSION_INT)
        dout.flush()
        this.dataOut = dout
    }

    actual protected constructor() {
        this.dataOut = null
    }

    private fun requireOut(): DataOutputStream = dataOut ?: throw IOException("Stream closed")

    actual override fun writeObject(obj: Any?) {
        val dout = requireOut()
        when (obj) {
            null -> dout.writeByte(TAG_NULL)
            is String -> {
                dout.writeByte(TAG_STRING)
                val bytes = obj.encodeToByteArray()
                dout.writeInt(bytes.size)
                dout.write(bytes)
            }
            is Boolean -> {
                dout.writeByte(TAG_BOOLEAN)
                dout.writeBoolean(obj)
            }
            is Byte -> {
                dout.writeByte(TAG_BYTE)
                dout.writeByte(obj.toInt())
            }
            is Short -> {
                dout.writeByte(TAG_SHORT)
                dout.writeShort(obj.toInt())
            }
            is Char -> {
                dout.writeByte(TAG_CHAR)
                dout.writeChar(obj.code)
            }
            is Int -> {
                dout.writeByte(TAG_INT)
                dout.writeInt(obj)
            }
            is Long -> {
                dout.writeByte(TAG_LONG)
                dout.writeLong(obj)
            }
            is Float -> {
                dout.writeByte(TAG_FLOAT)
                dout.writeFloat(obj)
            }
            is Double -> {
                dout.writeByte(TAG_DOUBLE)
                dout.writeDouble(obj)
            }
            is ByteArray -> {
                dout.writeByte(TAG_BYTE_ARRAY)
                dout.writeInt(obj.size)
                dout.write(obj)
            }
            is IntArray -> {
                dout.writeByte(TAG_INT_ARRAY)
                dout.writeInt(obj.size)
                for (v in obj) dout.writeInt(v)
            }
            is LongArray -> {
                dout.writeByte(TAG_LONG_ARRAY)
                dout.writeInt(obj.size)
                for (v in obj) dout.writeLong(v)
            }
            is Set<*> -> {
                dout.writeByte(TAG_SET)
                dout.writeInt(obj.size)
                for (item in obj) writeObject(item)
            }
            is List<*> -> {
                dout.writeByte(TAG_LIST)
                dout.writeInt(obj.size)
                for (item in obj) writeObject(item)
            }
            is Map<*, *> -> {
                dout.writeByte(TAG_MAP)
                dout.writeInt(obj.size)
                for ((k, v) in obj) {
                    writeObject(k)
                    writeObject(v)
                }
            }
            else -> throw NotSerializableException(obj::class.qualifiedName)
        }
    }

    actual override fun write(b: Int) {
        dataOut?.write(b)
    }

    actual override fun write(b: ByteArray) {
        dataOut?.write(b, 0, b.size)
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        dataOut?.write(b, off, len)
    }

    actual override fun flush() {
        dataOut?.flush()
    }

    actual open fun reset() {
        // Stateless stream format needs no handle reset
    }

    actual override fun close() {
        dataOut?.close()
    }

    actual override fun writeBoolean(v: Boolean) = requireOut().writeBoolean(v)
    actual override fun writeByte(v: Int) = requireOut().writeByte(v)
    actual override fun writeShort(v: Int) = requireOut().writeShort(v)
    actual override fun writeChar(v: Int) = requireOut().writeChar(v)
    actual override fun writeInt(v: Int) = requireOut().writeInt(v)
    actual override fun writeLong(v: Long) = requireOut().writeLong(v)
    actual override fun writeFloat(v: Float) = requireOut().writeFloat(v)
    actual override fun writeDouble(v: Double) = requireOut().writeDouble(v)
    actual override fun writeBytes(s: String) = requireOut().writeBytes(s)
    actual override fun writeChars(s: String) = requireOut().writeChars(s)
    actual override fun writeUTF(s: String) = requireOut().writeUTF(s)
}
