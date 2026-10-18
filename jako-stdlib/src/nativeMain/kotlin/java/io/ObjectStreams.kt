package java.io

private const val STREAM_MAGIC_INT: Int = 0xACED
private const val STREAM_VERSION_INT: Int = 0x0005

private const val TAG_NULL: Int = 0
private const val TAG_STRING: Int = 1
private const val TAG_BOOLEAN: Int = 2
private const val TAG_BYTE: Int = 3
private const val TAG_SHORT: Int = 4
private const val TAG_CHAR: Int = 5
private const val TAG_INT: Int = 6
private const val TAG_LONG: Int = 7
private const val TAG_FLOAT: Int = 8
private const val TAG_DOUBLE: Int = 9
private const val TAG_BYTE_ARRAY: Int = 10
private const val TAG_INT_ARRAY: Int = 11
private const val TAG_LONG_ARRAY: Int = 12
private const val TAG_LIST: Int = 13
private const val TAG_SET: Int = 14
private const val TAG_MAP: Int = 15

actual interface ObjectStreamConstants {
    actual companion object {
        actual val STREAM_MAGIC: Short = 0xaced.toShort()
        actual val STREAM_VERSION: Short = 5
        actual val TC_BASE: Byte = 0x70
        actual val TC_NULL: Byte = 0x70
        actual val TC_REFERENCE: Byte = 0x71
        actual val TC_CLASSDESC: Byte = 0x72
        actual val TC_OBJECT: Byte = 0x73
        actual val TC_STRING: Byte = 0x74
        actual val TC_ARRAY: Byte = 0x75
        actual val TC_CLASS: Byte = 0x76
        actual val TC_BLOCKDATA: Byte = 0x77
        actual val TC_ENDBLOCKDATA: Byte = 0x78
        actual val TC_RESET: Byte = 0x79
        actual val TC_BLOCKDATALONG: Byte = 0x7A
        actual val TC_EXCEPTION: Byte = 0x7B
        actual val TC_LONGSTRING: Byte = 0x7C
        actual val TC_PROXYCLASSDESC: Byte = 0x7D
        actual val TC_ENUM: Byte = 0x7E
        actual val TC_MAX: Byte = 0x7E
        actual val baseWireHandle: Int = 0x7e0000
        actual val SC_WRITE_METHOD: Byte = 0x01
        actual val SC_BLOCK_DATA: Byte = 0x08
        actual val SC_SERIALIZABLE: Byte = 0x02
        actual val SC_EXTERNALIZABLE: Byte = 0x04
        actual val SC_ENUM: Byte = 0x10
        actual val PROTOCOL_VERSION_1: Int = 1
        actual val PROTOCOL_VERSION_2: Int = 2
    }
}

actual interface ObjectInput : DataInput, AutoCloseable {
    actual fun readObject(): Any?
    actual fun read(): Int
    actual fun read(b: ByteArray): Int
    actual fun read(b: ByteArray, off: Int, len: Int): Int
    actual fun skip(n: Long): Long
    actual fun available(): Int
    actual override fun close()
}

actual interface ObjectOutput : DataOutput, AutoCloseable {
    actual fun writeObject(obj: Any?)
    actual override fun write(b: Int)
    actual override fun write(b: ByteArray)
    actual override fun write(b: ByteArray, off: Int, len: Int)
    @Throws(IOException::class)
    actual fun flush()
    actual override fun close()
}

actual interface Externalizable : Serializable {
    actual fun writeExternal(out: ObjectOutput)
    actual fun readExternal(inStream: ObjectInput)
}

actual open class ObjectInputStream : InputStream, ObjectInput, ObjectStreamConstants {
    private val dataIn: DataInputStream?

    actual constructor(inStream: InputStream) {
        val din = DataInputStream(inStream)
        val magic = din.readUnsignedShort()
        val version = din.readUnsignedShort()
        if (magic != STREAM_MAGIC_INT || version != STREAM_VERSION_INT) {
            throw StreamCorruptedException("Invalid stream header: ${magic.toString(16)} ${version.toString(16)}")
        }
        this.dataIn = din
    }

    actual protected constructor() {
        this.dataIn = null
    }

    private fun requireIn(): DataInputStream = dataIn ?: throw IOException("Stream closed")

    actual override fun readObject(): Any? {
        val din = requireIn()
        return when (val tag = din.readUnsignedByte()) {
            TAG_NULL -> null
            TAG_STRING -> {
                val len = din.readInt()
                val bytes = ByteArray(len)
                din.readFully(bytes)
                bytes.decodeToString()
            }
            TAG_BOOLEAN -> din.readBoolean()
            TAG_BYTE -> din.readByte()
            TAG_SHORT -> din.readShort()
            TAG_CHAR -> din.readChar()
            TAG_INT -> din.readInt()
            TAG_LONG -> din.readLong()
            TAG_FLOAT -> din.readFloat()
            TAG_DOUBLE -> din.readDouble()
            TAG_BYTE_ARRAY -> {
                val len = din.readInt()
                ByteArray(len).also { din.readFully(it) }
            }
            TAG_INT_ARRAY -> {
                val len = din.readInt()
                IntArray(len) { din.readInt() }
            }
            TAG_LONG_ARRAY -> {
                val len = din.readInt()
                LongArray(len) { din.readLong() }
            }
            TAG_LIST -> {
                val size = din.readInt()
                val list = ArrayList<Any?>(size)
                repeat(size) { list.add(readObject()) }
                list
            }
            TAG_SET -> {
                val size = din.readInt()
                val set = LinkedHashSet<Any?>(size)
                repeat(size) { set.add(readObject()) }
                set
            }
            TAG_MAP -> {
                val size = din.readInt()
                val map = LinkedHashMap<Any?, Any?>(size)
                repeat(size) {
                    val k = readObject()
                    val v = readObject()
                    map[k] = v
                }
                map
            }
            else -> throw StreamCorruptedException("Unsupported serialized object tag: $tag")
        }
    }

    actual override fun read(): Int = dataIn?.read() ?: -1

    override fun read(b: ByteArray): Int = read(b, 0, b.size)

    actual override fun read(b: ByteArray, off: Int, len: Int): Int = dataIn?.read(b, off, len) ?: -1

    override fun skip(n: Long): Long = dataIn?.skip(n) ?: 0L

    actual override fun available(): Int = dataIn?.available() ?: 0

    actual override fun close() {
        dataIn?.close()
    }

    actual override fun readBoolean(): Boolean = requireIn().readBoolean()
    actual override fun readByte(): Byte = requireIn().readByte()
    actual override fun readUnsignedByte(): Int = requireIn().readUnsignedByte()
    actual override fun readChar(): Char = requireIn().readChar()
    actual override fun readShort(): Short = requireIn().readShort()
    actual override fun readUnsignedShort(): Int = requireIn().readUnsignedShort()
    actual override fun readInt(): Int = requireIn().readInt()
    actual override fun readLong(): Long = requireIn().readLong()
    actual override fun readFloat(): Float = requireIn().readFloat()
    actual override fun readDouble(): Double = requireIn().readDouble()
    actual override fun readFully(b: ByteArray) = requireIn().readFully(b)
    actual override fun readFully(b: ByteArray, off: Int, len: Int) = requireIn().readFully(b, off, len)
    actual override fun skipBytes(n: Int): Int = requireIn().skipBytes(n)
    actual override fun readLine(): String? = requireIn().readLine()
    actual override fun readUTF(): String = requireIn().readUTF()
}

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
