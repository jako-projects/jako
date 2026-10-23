package java.io

internal const val STREAM_MAGIC_INT: Int = 0xACED

internal const val STREAM_VERSION_INT: Int = 0x0005

internal const val TAG_NULL: Int = 0

internal const val TAG_STRING: Int = 1

internal const val TAG_BOOLEAN: Int = 2

internal const val TAG_BYTE: Int = 3

internal const val TAG_SHORT: Int = 4

internal const val TAG_CHAR: Int = 5

internal const val TAG_INT: Int = 6

internal const val TAG_LONG: Int = 7

internal const val TAG_FLOAT: Int = 8

internal const val TAG_DOUBLE: Int = 9

internal const val TAG_BYTE_ARRAY: Int = 10

internal const val TAG_INT_ARRAY: Int = 11

internal const val TAG_LONG_ARRAY: Int = 12

internal const val TAG_LIST: Int = 13

internal const val TAG_SET: Int = 14

internal const val TAG_MAP: Int = 15

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
