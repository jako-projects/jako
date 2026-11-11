package java.io

import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.util.Date
import java.util.Locale
import java.util.UUID

private val JS_ERASED_NUMBERS: Boolean = run {
    val zero: Any = 0
    (zero is Byte) && (zero is Double)
}

internal class SerialFieldDesc(
    val typeCode: Char,
    val name: String,
    val typeString: String? = null
)

internal class SerialClassDesc(
    val name: String,
    val serialVersionUID: Long,
    val flags: Int,
    val fields: List<SerialFieldDesc> = emptyList(),
    val superDesc: SerialClassDesc? = null
)

internal object BuiltinSerialDescriptors {
    private const val SC_SER = 0x02
    private const val SC_SER_WRITE = 0x03 // SC_SERIALIZABLE | SC_WRITE_METHOD

    val NUMBER = SerialClassDesc("java.lang.Number", -8742448824652078965L, SC_SER)
    val BOOLEAN = SerialClassDesc("java.lang.Boolean", -3665804199014368530L, SC_SER, listOf(SerialFieldDesc('Z', "value")))
    val BYTE = SerialClassDesc("java.lang.Byte", -7183698231559129828L, SC_SER, listOf(SerialFieldDesc('B', "value")), NUMBER)
    val SHORT = SerialClassDesc("java.lang.Short", 7515723908773894738L, SC_SER, listOf(SerialFieldDesc('S', "value")), NUMBER)
    val CHARACTER = SerialClassDesc("java.lang.Character", 3786198910865385080L, SC_SER, listOf(SerialFieldDesc('C', "value")))
    val INTEGER = SerialClassDesc("java.lang.Integer", 1360826667806852920L, SC_SER, listOf(SerialFieldDesc('I', "value")), NUMBER)
    val LONG = SerialClassDesc("java.lang.Long", 4290774380558885855L, SC_SER, listOf(SerialFieldDesc('J', "value")), NUMBER)
    val FLOAT = SerialClassDesc("java.lang.Float", -2671257302660747028L, SC_SER, listOf(SerialFieldDesc('F', "value")), NUMBER)
    val DOUBLE = SerialClassDesc("java.lang.Double", -9172774392245257468L, SC_SER, listOf(SerialFieldDesc('D', "value")), NUMBER)

    val BYTE_ARRAY = SerialClassDesc("[B", -5984413125824719648L, SC_SER)
    val BOOLEAN_ARRAY = SerialClassDesc("[Z", 6309297032502205922L, SC_SER)
    val SHORT_ARRAY = SerialClassDesc("[S", -1188055269542874886L, SC_SER)
    val CHAR_ARRAY = SerialClassDesc("[C", -5753798564021173076L, SC_SER)
    val INT_ARRAY = SerialClassDesc("[I", 5600894804908749477L, SC_SER)
    val LONG_ARRAY = SerialClassDesc("[J", 8655923659555304851L, SC_SER)
    val FLOAT_ARRAY = SerialClassDesc("[F", 836686056779680834L, SC_SER)
    val DOUBLE_ARRAY = SerialClassDesc("[D", 4514449696888150558L, SC_SER)
    val OBJECT_ARRAY = SerialClassDesc("[Ljava.lang.Object;", -8012369246846506644L, SC_SER)

    val ARRAY_LIST = SerialClassDesc(
        "java.util.ArrayList",
        8683452581122892189L,
        SC_SER_WRITE,
        listOf(SerialFieldDesc('I', "size"))
    )
    val LINKED_LIST = SerialClassDesc("java.util.LinkedList", 876323262645176354L, SC_SER_WRITE)
    val HASH_SET = SerialClassDesc("java.util.HashSet", -5024744406713321676L, SC_SER_WRITE)
    val LINKED_HASH_SET = SerialClassDesc("java.util.LinkedHashSet", -2851667679971038690L, SC_SER, emptyList(), HASH_SET)
    val HASH_MAP = SerialClassDesc(
        "java.util.HashMap",
        362498820763181265L,
        SC_SER_WRITE,
        listOf(SerialFieldDesc('F', "loadFactor"), SerialFieldDesc('I', "threshold"))
    )
    val LINKED_HASH_MAP = SerialClassDesc(
        "java.util.LinkedHashMap",
        3801124242820219131L,
        SC_SER,
        listOf(SerialFieldDesc('Z', "accessOrder")),
        HASH_MAP
    )

    val DATE = SerialClassDesc("java.util.Date", 7523967970034938905L, SC_SER_WRITE)
    val UUID_DESC = SerialClassDesc(
        "java.util.UUID",
        -4856846361193249489L,
        SC_SER,
        listOf(SerialFieldDesc('J', "leastSigBits"), SerialFieldDesc('J', "mostSigBits"))
    )
    val FILE = SerialClassDesc(
        "java.io.File",
        301077366599181567L,
        SC_SER_WRITE,
        listOf(SerialFieldDesc('L', "path", "Ljava/lang/String;"))
    )
    val URI_DESC = SerialClassDesc(
        "java.net.URI",
        -6052424284110960213L,
        SC_SER_WRITE,
        listOf(SerialFieldDesc('L', "string", "Ljava/lang/String;"))
    )
    val BIG_INTEGER = SerialClassDesc(
        "java.math.BigInteger",
        -8287574255936472291L,
        SC_SER_WRITE,
        listOf(
            SerialFieldDesc('I', "bitCount"),
            SerialFieldDesc('I', "bitLength"),
            SerialFieldDesc('I', "firstNonzeroByteNum"),
            SerialFieldDesc('I', "lowestSetBit"),
            SerialFieldDesc('I', "signum"),
            SerialFieldDesc('[', "magnitude", "[B")
        ),
        NUMBER
    )
    val BIG_DECIMAL = SerialClassDesc(
        "java.math.BigDecimal",
        6108874887143696463L,
        SC_SER_WRITE,
        listOf(
            SerialFieldDesc('I', "scale"),
            SerialFieldDesc('L', "intVal", "Ljava/math/BigInteger;")
        ),
        NUMBER
    )
    val LOCALE = SerialClassDesc(
        "java.util.Locale",
        9149081749638150636L,
        SC_SER_WRITE,
        listOf(
            SerialFieldDesc('I', "hashcode"),
            SerialFieldDesc('L', "country", "Ljava/lang/String;"),
            SerialFieldDesc('L', "extensions", "Ljava/lang/String;"),
            SerialFieldDesc('L', "language", "Ljava/lang/String;"),
            SerialFieldDesc('L', "script", "Ljava/lang/String;"),
            SerialFieldDesc('L', "variant", "Ljava/lang/String;")
        )
    )

    fun hashMapBuckets(size: Int): Int {
        if (size <= 0) return 16
        val desired = ((size.toFloat() / 0.75f).toInt() + 1).coerceAtLeast(16)
        var cap = 16
        while (cap < desired && cap < (1 shl 30)) {
            cap = cap shl 1
        }
        return cap
    }
}

actual open class ObjectOutputStream : OutputStream, ObjectOutput, ObjectStreamConstants {
    private val rawOut: DataOutputStream?
    private val blockBuf = ByteArray(MAX_BLOCK_SIZE)
    private var blockPos = 0
    private var hasWrittenObjects = false
    private var protocolVersion = ObjectStreamConstants.PROTOCOL_VERSION_2

    private var nextHandle = ObjectStreamConstants.baseWireHandle
    private val descHandles = HashMap<String, Int>()
    private val stringHandles = HashMap<String, Int>()
    private val identityBuckets = HashMap<Int, MutableList<Pair<Any, Int>>>()

    actual constructor(out: OutputStream) {
        val dout = DataOutputStream(out)
        dout.writeShort(STREAM_MAGIC_INT)
        dout.writeShort(STREAM_VERSION_INT)
        dout.flush()
        this.rawOut = dout
    }

    actual protected constructor() {
        this.rawOut = null
    }

    private fun requireOut(): DataOutputStream = rawOut ?: throw IOException("Stream closed")

    actual open fun useProtocolVersion(version: Int) {
        if (hasWrittenObjects) {
            throw IllegalStateException("Cannot change protocol version after objects have been written")
        }
        if (version != ObjectStreamConstants.PROTOCOL_VERSION_1 && version != ObjectStreamConstants.PROTOCOL_VERSION_2) {
            throw IllegalArgumentException("Unknown protocol version: $version")
        }
        this.protocolVersion = version
    }

    actual open fun defaultWriteObject() {
        throw NotActiveException("not in call to writeObject")
    }

    actual override fun writeObject(obj: Any?) {
        writeObjectInternal(obj, unshared = false)
    }

    actual open fun writeUnshared(obj: Any?) {
        writeObjectInternal(obj, unshared = true)
    }

    private fun drainBlockData() {
        val dout = rawOut ?: return
        if (blockPos == 0) return
        if (blockPos <= 255) {
            dout.writeByte(ObjectStreamConstants.TC_BLOCKDATA.toInt())
            dout.writeByte(blockPos)
        } else {
            dout.writeByte(ObjectStreamConstants.TC_BLOCKDATALONG.toInt())
            dout.writeInt(blockPos)
        }
        dout.write(blockBuf, 0, blockPos)
        blockPos = 0
    }

    private fun writeBlockByte(v: Int) {
        requireOut()
        if (blockPos == MAX_BLOCK_SIZE) {
            drainBlockData()
        }
        blockBuf[blockPos++] = v.toByte()
    }

    private fun writeBlockBytes(b: ByteArray, off: Int, len: Int) {
        requireOut()
        if (off < 0 || len < 0 || off + len > b.size) {
            throw IndexOutOfBoundsException("off=$off, len=$len, size=${b.size}")
        }
        var curOff = off
        var rem = len
        while (rem > 0) {
            if (blockPos == MAX_BLOCK_SIZE) {
                drainBlockData()
            }
            val chunk = minOf(rem, MAX_BLOCK_SIZE - blockPos)
            b.copyInto(blockBuf, blockPos, curOff, curOff + chunk)
            blockPos += chunk
            curOff += chunk
            rem -= chunk
        }
    }

    private fun identityBucketKey(obj: Any): Int {
        return when (obj) {
            is Collection<*> -> obj::class.hashCode() * 31 + obj.size
            is Map<*, *> -> obj::class.hashCode() * 31 + obj.size
            else -> obj.hashCode()
        }
    }

    private fun lookupObjectHandle(obj: Any): Int {
        val list = identityBuckets[identityBucketKey(obj)] ?: return -1
        for ((ref, handle) in list) {
            if (ref === obj) return handle
        }
        return -1
    }

    private fun registerObjectHandle(obj: Any, unshared: Boolean): Int {
        val handle = nextHandle++
        if (!unshared) {
            val key = identityBucketKey(obj)
            val list = identityBuckets.getOrPut(key) { ArrayList(2) }
            list.add(obj to handle)
        }
        return handle
    }

    private fun writeRawUtf(dout: DataOutputStream, s: String) {
        val bytes = ModifiedUtf8.encode(s)
        dout.writeShort(bytes.size)
        dout.write(bytes, 0, bytes.size)
    }

    private fun writeStringObject(dout: DataOutputStream, s: String, unshared: Boolean) {
        if (!unshared) {
            val existing = stringHandles[s]
            if (existing != null) {
                dout.writeByte(ObjectStreamConstants.TC_REFERENCE.toInt())
                dout.writeInt(existing)
                return
            }
        }
        val handle = nextHandle++
        if (!unshared) {
            stringHandles[s] = handle
        }
        val utfLen = ModifiedUtf8.encodedLength(s)
        if (utfLen <= 65535L) {
            val bytes = ModifiedUtf8.encodeUnchecked(s, utfLen.toInt())
            dout.writeByte(ObjectStreamConstants.TC_STRING.toInt())
            dout.writeShort(bytes.size)
            dout.write(bytes, 0, bytes.size)
        } else {
            val bytes = ModifiedUtf8.encodeUnchecked(s, utfLen.toInt())
            dout.writeByte(ObjectStreamConstants.TC_LONGSTRING.toInt())
            dout.writeLong(utfLen)
            dout.write(bytes, 0, bytes.size)
        }
    }

    private fun writeClassDesc(dout: DataOutputStream, desc: SerialClassDesc?) {
        if (desc == null) {
            dout.writeByte(ObjectStreamConstants.TC_NULL.toInt())
            return
        }
        val existing = descHandles[desc.name]
        if (existing != null) {
            dout.writeByte(ObjectStreamConstants.TC_REFERENCE.toInt())
            dout.writeInt(existing)
            return
        }
        dout.writeByte(ObjectStreamConstants.TC_CLASSDESC.toInt())
        writeRawUtf(dout, desc.name)
        dout.writeLong(desc.serialVersionUID)
        // Spec 6.4.2: ClassDesc wire handle is assigned BEFORE field type strings & superClassDesc
        val handle = nextHandle++
        descHandles[desc.name] = handle

        dout.writeByte(desc.flags)
        dout.writeShort(desc.fields.size)
        for (f in desc.fields) {
            dout.writeByte(f.typeCode.code)
            writeRawUtf(dout, f.name)
            if (f.typeCode == 'L' || f.typeCode == '[') {
                writeStringObject(dout, f.typeString!!, unshared = false)
            }
        }
        dout.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA.toInt())
        writeClassDesc(dout, desc.superDesc)
    }

    private fun writeEndBlockData(dout: DataOutputStream) {
        drainBlockData()
        dout.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA.toInt())
    }

    private fun writeObjectInternal(obj: Any?, unshared: Boolean) {
        val dout = requireOut()
        drainBlockData()
        hasWrittenObjects = true

        if (obj == null) {
            dout.writeByte(ObjectStreamConstants.TC_NULL.toInt())
            return
        }

        if (obj is String) {
            writeStringObject(dout, obj, unshared)
            return
        }

        // Boxed primitives (value types in Kotlin; write fresh TC_OBJECT with shared ClassDesc).
        // Note: On Kotlin/JS, Byte/Short/Int/Float/Double all erase to JS `number` when boxed as Any,
        // whereas Boolean, Char, and Long retain distinct runtime representations.
        if (obj is Boolean) {
            dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
            writeClassDesc(dout, BuiltinSerialDescriptors.BOOLEAN)
            nextHandle++
            dout.writeBoolean(obj)
            return
        }
        if (obj is Char) {
            dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
            writeClassDesc(dout, BuiltinSerialDescriptors.CHARACTER)
            nextHandle++
            dout.writeChar(obj.code)
            return
        }
        if (obj is Long) {
            dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
            writeClassDesc(dout, BuiltinSerialDescriptors.LONG)
            nextHandle++
            dout.writeLong(obj)
            return
        }
        if (JS_ERASED_NUMBERS) {
            if (obj is Double) {
                val isNegativeZero = obj == 0.0 && (1.0 / obj) < 0.0
                if (!isNegativeZero && obj.isFinite() &&
                    obj >= Int.MIN_VALUE.toDouble() && obj <= Int.MAX_VALUE.toDouble() &&
                    obj.toInt().toDouble() == obj
                ) {
                    dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                    writeClassDesc(dout, BuiltinSerialDescriptors.INTEGER)
                    nextHandle++
                    dout.writeInt(obj.toInt())
                } else {
                    dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                    writeClassDesc(dout, BuiltinSerialDescriptors.DOUBLE)
                    nextHandle++
                    dout.writeDouble(obj)
                }
                return
            }
        } else {
            when (obj) {
                is Byte -> {
                    dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                    writeClassDesc(dout, BuiltinSerialDescriptors.BYTE)
                    nextHandle++
                    dout.writeByte(obj.toInt())
                    return
                }
                is Short -> {
                    dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                    writeClassDesc(dout, BuiltinSerialDescriptors.SHORT)
                    nextHandle++
                    dout.writeShort(obj.toInt())
                    return
                }
                is Int -> {
                    dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                    writeClassDesc(dout, BuiltinSerialDescriptors.INTEGER)
                    nextHandle++
                    dout.writeInt(obj)
                    return
                }
                is Float -> {
                    dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                    writeClassDesc(dout, BuiltinSerialDescriptors.FLOAT)
                    nextHandle++
                    dout.writeFloat(obj)
                    return
                }
                is Double -> {
                    dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                    writeClassDesc(dout, BuiltinSerialDescriptors.DOUBLE)
                    nextHandle++
                    dout.writeDouble(obj)
                    return
                }
            }
        }

        if (!unshared) {
            val existing = lookupObjectHandle(obj)
            if (existing != -1) {
                dout.writeByte(ObjectStreamConstants.TC_REFERENCE.toInt())
                dout.writeInt(existing)
                return
            }
        }

        when (obj) {
            is ByteArray -> {
                dout.writeByte(ObjectStreamConstants.TC_ARRAY.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.BYTE_ARRAY)
                registerObjectHandle(obj, unshared)
                dout.writeInt(obj.size)
                dout.write(obj, 0, obj.size)
            }
            is BooleanArray -> {
                dout.writeByte(ObjectStreamConstants.TC_ARRAY.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.BOOLEAN_ARRAY)
                registerObjectHandle(obj, unshared)
                dout.writeInt(obj.size)
                for (v in obj) dout.writeBoolean(v)
            }
            is ShortArray -> {
                dout.writeByte(ObjectStreamConstants.TC_ARRAY.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.SHORT_ARRAY)
                registerObjectHandle(obj, unshared)
                dout.writeInt(obj.size)
                for (v in obj) dout.writeShort(v.toInt())
            }
            is CharArray -> {
                dout.writeByte(ObjectStreamConstants.TC_ARRAY.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.CHAR_ARRAY)
                registerObjectHandle(obj, unshared)
                dout.writeInt(obj.size)
                for (v in obj) dout.writeChar(v.code)
            }
            is IntArray -> {
                dout.writeByte(ObjectStreamConstants.TC_ARRAY.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.INT_ARRAY)
                registerObjectHandle(obj, unshared)
                dout.writeInt(obj.size)
                for (v in obj) dout.writeInt(v)
            }
            is LongArray -> {
                dout.writeByte(ObjectStreamConstants.TC_ARRAY.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.LONG_ARRAY)
                registerObjectHandle(obj, unshared)
                dout.writeInt(obj.size)
                for (v in obj) dout.writeLong(v)
            }
            is FloatArray -> {
                dout.writeByte(ObjectStreamConstants.TC_ARRAY.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.FLOAT_ARRAY)
                registerObjectHandle(obj, unshared)
                dout.writeInt(obj.size)
                for (v in obj) dout.writeFloat(v)
            }
            is DoubleArray -> {
                dout.writeByte(ObjectStreamConstants.TC_ARRAY.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.DOUBLE_ARRAY)
                registerObjectHandle(obj, unshared)
                dout.writeInt(obj.size)
                for (v in obj) dout.writeDouble(v)
            }
            is Array<*> -> {
                dout.writeByte(ObjectStreamConstants.TC_ARRAY.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.OBJECT_ARRAY)
                registerObjectHandle(obj, unshared)
                dout.writeInt(obj.size)
                for (elem in obj) writeObjectInternal(elem, unshared = false)
            }
            is java.util.LinkedList<*> -> {
                dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.LINKED_LIST)
                registerObjectHandle(obj, unshared)
                writeInt(obj.size)
                for (elem in obj) writeObjectInternal(elem, unshared = false)
                writeEndBlockData(dout)
            }
            is List<*> -> {
                dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.ARRAY_LIST)
                registerObjectHandle(obj, unshared)
                dout.writeInt(obj.size) // field: int size
                writeInt(obj.size) // block data: int capacity
                for (elem in obj) writeObjectInternal(elem, unshared = false)
                writeEndBlockData(dout)
            }
            is Set<*> -> {
                dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.LINKED_HASH_SET)
                registerObjectHandle(obj, unshared)
                writeHashSetData(dout, obj)
            }
            is java.util.LinkedHashMap<*, *> -> {
                dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.LINKED_HASH_MAP)
                registerObjectHandle(obj, unshared)
                writeHashMapSlot(dout, obj)
                // LinkedHashMap subclass slot: boolean accessOrder
                dout.writeBoolean(obj.accessOrder)
            }
            is kotlin.collections.HashMap<*, *> -> {
                dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.HASH_MAP)
                registerObjectHandle(obj, unshared)
                writeHashMapSlot(dout, obj)
            }
            is Map<*, *> -> {
                dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.LINKED_HASH_MAP)
                registerObjectHandle(obj, unshared)
                writeHashMapSlot(dout, obj)
                dout.writeBoolean(false)
            }
            is Date -> {
                dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.DATE)
                registerObjectHandle(obj, unshared)
                writeLong(obj.time)
                writeEndBlockData(dout)
            }
            is UUID -> {
                dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.UUID_DESC)
                registerObjectHandle(obj, unshared)
                dout.writeLong(obj.leastSignificantBits)
                dout.writeLong(obj.mostSignificantBits)
            }
            is File -> {
                dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.FILE)
                registerObjectHandle(obj, unshared)
                writeStringObject(dout, obj.path, unshared = false)
                writeChar(File.separatorChar.code)
                writeEndBlockData(dout)
            }
            is URI -> {
                dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.URI_DESC)
                registerObjectHandle(obj, unshared)
                writeStringObject(dout, obj.toString(), unshared = false)
                writeEndBlockData(dout)
            }
            is BigInteger -> {
                dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.BIG_INTEGER)
                registerObjectHandle(obj, unshared)
                val sig = obj.signum()
                val mag = if (sig == 0) {
                    ByteArray(0)
                } else {
                    val raw = obj.abs().toByteArray()
                    var firstNonZero = 0
                    while (firstNonZero < raw.size && raw[firstNonZero] == 0.toByte()) {
                        firstNonZero++
                    }
                    if (firstNonZero == 0) raw else raw.copyOfRange(firstNonZero, raw.size)
                }
                dout.writeInt(-1) // bitCount
                dout.writeInt(-1) // bitLength
                dout.writeInt(-2) // firstNonzeroByteNum
                dout.writeInt(-2) // lowestSetBit
                dout.writeInt(sig) // signum
                writeObjectInternal(mag, unshared = false) // magnitude: byte[]
                writeEndBlockData(dout)
            }
            is BigDecimal -> {
                dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.BIG_DECIMAL)
                registerObjectHandle(obj, unshared)
                dout.writeInt(obj.scale())
                writeObjectInternal(obj.unscaledValue(), unshared = false)
                writeEndBlockData(dout)
            }
            is Locale -> {
                dout.writeByte(ObjectStreamConstants.TC_OBJECT.toInt())
                writeClassDesc(dout, BuiltinSerialDescriptors.LOCALE)
                registerObjectHandle(obj, unshared)
                dout.writeInt(-1) // hashcode
                writeStringObject(dout, obj.language.let { _ -> obj.country }, unshared = false) // country
                writeStringObject(dout, "", unshared = false) // extensions
                writeStringObject(dout, obj.language, unshared = false) // language
                writeStringObject(dout, "", unshared = false) // script
                writeStringObject(dout, obj.variant, unshared = false) // variant
                writeEndBlockData(dout)
            }
            else -> throw NotSerializableException(obj::class.simpleName ?: "Unknown")
        }
    }

    private fun writeHashSetData(dout: DataOutputStream, set: Set<*>) {
        val buckets = BuiltinSerialDescriptors.hashMapBuckets(set.size)
        writeInt(buckets)
        writeFloat(0.75f)
        writeInt(set.size)
        for (elem in set) {
            writeObjectInternal(elem, unshared = false)
        }
        writeEndBlockData(dout)
    }

    private fun writeHashMapSlot(dout: DataOutputStream, map: Map<*, *>) {
        val buckets = BuiltinSerialDescriptors.hashMapBuckets(map.size)
        val threshold = if (map.isEmpty()) 0 else (buckets * 0.75f).toInt()
        dout.writeFloat(0.75f) // loadFactor
        dout.writeInt(threshold) // threshold
        writeInt(buckets) // block data: buckets
        writeInt(map.size) // block data: size
        for ((k, v) in map) {
            writeObjectInternal(k, unshared = false)
            writeObjectInternal(v, unshared = false)
        }
        writeEndBlockData(dout)
    }

    actual override fun write(b: Int) {
        if (rawOut == null) return
        writeBlockByte(b)
    }

    actual override fun write(b: ByteArray) {
        write(b, 0, b.size)
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        if (rawOut == null) return
        writeBlockBytes(b, off, len)
    }

    actual override fun flush() {
        val dout = rawOut ?: return
        drainBlockData()
        dout.flush()
    }

    actual open fun reset() {
        val dout = requireOut()
        drainBlockData()
        dout.writeByte(ObjectStreamConstants.TC_RESET.toInt())
        nextHandle = ObjectStreamConstants.baseWireHandle
        descHandles.clear()
        stringHandles.clear()
        identityBuckets.clear()
    }

    actual override fun close() {
        val dout = rawOut ?: return
        drainBlockData()
        dout.close()
    }

    actual override fun writeBoolean(v: Boolean) {
        writeBlockByte(if (v) 1 else 0)
    }

    actual override fun writeByte(v: Int) {
        writeBlockByte(v)
    }

    actual override fun writeShort(v: Int) {
        writeBlockByte((v ushr 8) and 0xFF)
        writeBlockByte(v and 0xFF)
    }

    actual override fun writeChar(v: Int) {
        writeBlockByte((v ushr 8) and 0xFF)
        writeBlockByte(v and 0xFF)
    }

    actual override fun writeInt(v: Int) {
        writeBlockByte((v ushr 24) and 0xFF)
        writeBlockByte((v ushr 16) and 0xFF)
        writeBlockByte((v ushr 8) and 0xFF)
        writeBlockByte(v and 0xFF)
    }

    actual override fun writeLong(v: Long) {
        writeBlockByte(((v ushr 56) and 0xFFL).toInt())
        writeBlockByte(((v ushr 48) and 0xFFL).toInt())
        writeBlockByte(((v ushr 40) and 0xFFL).toInt())
        writeBlockByte(((v ushr 32) and 0xFFL).toInt())
        writeBlockByte(((v ushr 24) and 0xFFL).toInt())
        writeBlockByte(((v ushr 16) and 0xFFL).toInt())
        writeBlockByte(((v ushr 8) and 0xFFL).toInt())
        writeBlockByte((v and 0xFFL).toInt())
    }

    actual override fun writeFloat(v: Float) = writeInt(v.toRawBits())

    actual override fun writeDouble(v: Double) = writeLong(v.toRawBits())

    actual override fun writeBytes(s: String) {
        for (i in 0 until s.length) {
            writeBlockByte(s[i].code and 0xFF)
        }
    }

    actual override fun writeChars(s: String) {
        for (i in 0 until s.length) {
            writeChar(s[i].code)
        }
    }

    actual override fun writeUTF(s: String) {
        val bytes = ModifiedUtf8.encode(s)
        writeShort(bytes.size)
        writeBlockBytes(bytes, 0, bytes.size)
    }

    companion object {
        private const val MAX_BLOCK_SIZE = 1024
    }
}
