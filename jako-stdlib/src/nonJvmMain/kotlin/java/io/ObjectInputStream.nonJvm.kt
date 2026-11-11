package java.io

import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.util.Date
import java.util.Locale
import java.util.UUID

internal const val STREAM_MAGIC_INT: Int = 0xACED
internal const val STREAM_VERSION_INT: Int = 0x0005

private object UnsharedMarker

actual open class ObjectInputStream : InputStream, ObjectInput, ObjectStreamConstants {
    private val rawIn: InputStream?
    private var peekedByte: Int = -1
    private var unreadBlockBytes: Int = 0
    private val handles = ArrayList<Any?>()
    private val unsharedHandles = HashSet<Int>()

    actual constructor(inStream: InputStream) {
        this.rawIn = inStream
        val magic = rawReadUnsignedShort()
        val version = rawReadUnsignedShort()
        if (magic != STREAM_MAGIC_INT || version != STREAM_VERSION_INT) {
            throw StreamCorruptedException("Invalid stream header: ${magic.toString(16)} ${version.toString(16)}")
        }
    }

    actual protected constructor() {
        this.rawIn = null
    }

    private fun requireStream(): InputStream = rawIn ?: throw IOException("Stream closed")

    actual open fun defaultReadObject() {
        throw NotActiveException("not in call to readObject")
    }

    actual override fun readObject(): Any? = readObjectInternal(unshared = false)

    actual open fun readUnshared(): Any? = readObjectInternal(unshared = true)

    private fun resetHandles() {
        handles.clear()
        unsharedHandles.clear()
    }

    private fun assignHandle(value: Any?, unshared: Boolean): Int {
        val idx = handles.size
        handles.add(if (unshared) UnsharedMarker else value)
        if (unshared) {
            unsharedHandles.add(idx)
        }
        return idx
    }

    private fun setHandleValue(idx: Int, value: Any?, unshared: Boolean) {
        if (!unshared) {
            handles[idx] = value
        }
    }

    private fun resolveHandle(wireHandle: Int): Any? {
        val idx = wireHandle - ObjectStreamConstants.baseWireHandle
        if (idx < 0 || idx >= handles.size) {
            throw StreamCorruptedException("Invalid wire handle: 0x${wireHandle.toString(16)}")
        }
        if (idx in unsharedHandles || handles[idx] === UnsharedMarker) {
            throw InvalidObjectException("cannot read back reference to unshared object")
        }
        return handles[idx]
    }

    private fun rawPeek(): Int {
        val s = requireStream()
        if (peekedByte >= 0) return peekedByte
        val b = s.read()
        if (b >= 0) {
            peekedByte = b
        }
        return b
    }

    private fun rawRead(): Int {
        val s = requireStream()
        if (peekedByte >= 0) {
            val b = peekedByte
            peekedByte = -1
            return b
        }
        return s.read()
    }

    private fun rawReadUnsignedByte(): Int {
        val b = rawRead()
        if (b < 0) throw EOFException()
        return b
    }

    private fun rawReadFully(b: ByteArray, off: Int = 0, len: Int = b.size - off) {
        if (len < 0 || off < 0 || off + len > b.size) throw IndexOutOfBoundsException()
        var n = 0
        if (n < len && peekedByte >= 0) {
            b[off] = peekedByte.toByte()
            peekedByte = -1
            n = 1
        }
        val s = requireStream()
        while (n < len) {
            val count = s.read(b, off + n, len - n)
            if (count < 0) throw EOFException()
            n += count
        }
    }

    private fun rawReadByteArrayChunked(totalLen: Int): ByteArray {
        if (totalLen < 0) throw StreamCorruptedException("Negative length: $totalLen")
        if (totalLen <= INITIAL_CHUNK_LIMIT) {
            return ByteArray(totalLen).also { rawReadFully(it) }
        }
        val chunks = ArrayList<ByteArray>()
        var remaining = totalLen
        while (remaining > 0) {
            val chunkLen = minOf(remaining, INITIAL_CHUNK_LIMIT)
            val chunk = ByteArray(chunkLen)
            rawReadFully(chunk)
            chunks.add(chunk)
            remaining -= chunkLen
        }
        val out = ByteArray(totalLen)
        var pos = 0
        for (c in chunks) {
            c.copyInto(out, pos)
            pos += c.size
        }
        return out
    }

    private fun rawReadBoolean(): Boolean = rawReadUnsignedByte() != 0
    private fun rawReadByte(): Byte = rawReadUnsignedByte().toByte()

    private fun rawReadShort(): Short {
        val ch1 = rawReadUnsignedByte()
        val ch2 = rawReadUnsignedByte()
        return ((ch1 shl 8) or ch2).toShort()
    }

    private fun rawReadUnsignedShort(): Int {
        val ch1 = rawReadUnsignedByte()
        val ch2 = rawReadUnsignedByte()
        return (ch1 shl 8) or ch2
    }

    private fun rawReadChar(): Char = rawReadUnsignedShort().toChar()

    private fun rawReadInt(): Int {
        val ch1 = rawReadUnsignedByte()
        val ch2 = rawReadUnsignedByte()
        val ch3 = rawReadUnsignedByte()
        val ch4 = rawReadUnsignedByte()
        return (ch1 shl 24) or (ch2 shl 16) or (ch3 shl 8) or ch4
    }

    private fun rawReadLong(): Long {
        val hi = rawReadInt().toLong()
        val lo = rawReadInt().toLong() and 0xFFFFFFFFL
        return (hi shl 32) or lo
    }

    private fun rawReadFloat(): Float = Float.fromBits(rawReadInt())
    private fun rawReadDouble(): Double = Double.fromBits(rawReadLong())

    private fun rawReadUtf(): String {
        val len = rawReadUnsignedShort()
        val bytes = ByteArray(len)
        rawReadFully(bytes)
        return ModifiedUtf8.decode(bytes, 0, len)
    }

    private fun refillBlockData(): Boolean {
        while (unreadBlockBytes == 0) {
            val next = rawPeek()
            if (next < 0) return false
            when (next.toByte()) {
                ObjectStreamConstants.TC_BLOCKDATA -> {
                    rawRead()
                    unreadBlockBytes = rawReadUnsignedByte()
                }
                ObjectStreamConstants.TC_BLOCKDATALONG -> {
                    rawRead()
                    val len = rawReadInt()
                    if (len < 0) throw StreamCorruptedException("Negative block data length: $len")
                    unreadBlockBytes = len
                }
                ObjectStreamConstants.TC_RESET -> {
                    rawRead()
                    resetHandles()
                }
                else -> return false
            }
        }
        return true
    }

    private fun readBlockByteOrEof(): Int {
        if (!refillBlockData()) return -1
        val b = rawRead()
        if (b < 0) throw EOFException()
        unreadBlockBytes--
        return b
    }

    private fun readBlockUnsignedByte(): Int {
        val b = readBlockByteOrEof()
        if (b < 0) throw EOFException()
        return b
    }

    private fun readStringToken(tag: Byte, unshared: Boolean): String {
        val bytes = when (tag) {
            ObjectStreamConstants.TC_STRING -> {
                val len = rawReadUnsignedShort()
                ByteArray(len).also { rawReadFully(it) }
            }
            ObjectStreamConstants.TC_LONGSTRING -> {
                val lenLong = rawReadLong()
                if (lenLong < 0L || lenLong > Int.MAX_VALUE.toLong()) {
                    throw StreamCorruptedException("Invalid long string length: $lenLong")
                }
                rawReadByteArrayChunked(lenLong.toInt())
            }
            else -> throw StreamCorruptedException("Expected string tag, got 0x${(tag.toInt() and 0xFF).toString(16)}")
        }
        val str = ModifiedUtf8.decode(bytes, 0, bytes.size)
        assignHandle(str, unshared)
        return str
    }

    private fun readFieldTypeString(): String {
        while (true) {
            val tc = rawReadUnsignedByte().toByte()
            when (tc) {
                ObjectStreamConstants.TC_RESET -> resetHandles()
                ObjectStreamConstants.TC_REFERENCE -> {
                    val ref = resolveHandle(rawReadInt())
                    return ref as? String ?: throw StreamCorruptedException("Field type reference is not a String")
                }
                ObjectStreamConstants.TC_STRING, ObjectStreamConstants.TC_LONGSTRING -> {
                    return readStringToken(tc, unshared = false)
                }
                else -> throw StreamCorruptedException("Invalid field type token: 0x${(tc.toInt() and 0xFF).toString(16)}")
            }
        }
    }

    private fun readClassDesc(): SerialClassDesc? {
        while (true) {
            val tc = rawReadUnsignedByte().toByte()
            when (tc) {
                ObjectStreamConstants.TC_NULL -> return null
                ObjectStreamConstants.TC_RESET -> resetHandles()
                ObjectStreamConstants.TC_REFERENCE -> {
                    val ref = resolveHandle(rawReadInt())
                    return ref as? SerialClassDesc ?: throw StreamCorruptedException("Expected ClassDesc handle")
                }
                ObjectStreamConstants.TC_CLASSDESC -> {
                    val name = rawReadUtf()
                    val uid = rawReadLong()
                    // Reserve handle BEFORE reading field type strings & superClassDesc
                    val handleIdx = assignHandle(null, unshared = false)
                    val flags = rawReadUnsignedByte()
                    val fieldCount = rawReadUnsignedShort()
                    val fields = ArrayList<SerialFieldDesc>(minOf(fieldCount, 64))
                    repeat(fieldCount) {
                        val typeCode = rawReadUnsignedByte().toChar()
                        val fieldName = rawReadUtf()
                        val typeStr = if (typeCode == 'L' || typeCode == '[') {
                            readFieldTypeString()
                        } else {
                            null
                        }
                        fields.add(SerialFieldDesc(typeCode, fieldName, typeStr))
                    }
                    skipClassAnnotations()
                    val superDesc = readClassDesc()
                    val desc = SerialClassDesc(name, uid, flags, fields, superDesc)
                    setHandleValue(handleIdx, desc, unshared = false)
                    return desc
                }
                ObjectStreamConstants.TC_PROXYCLASSDESC -> {
                    throw InvalidClassException("Dynamic proxy deserialization is not supported on non-JVM targets")
                }
                else -> throw StreamCorruptedException("Invalid ClassDesc token: 0x${(tc.toInt() and 0xFF).toString(16)}")
            }
        }
    }

    private fun skipClassAnnotations() {
        skipCustomDataToEndBlock()
    }

    private fun skipCustomDataToEndBlock() {
        while (true) {
            if (unreadBlockBytes > 0) {
                while (unreadBlockBytes > 0) {
                    rawReadUnsignedByte()
                    unreadBlockBytes--
                }
            }
            val next = rawPeek()
            if (next < 0) throw EOFException("Unexpected EOF before TC_ENDBLOCKDATA")
            when (next.toByte()) {
                ObjectStreamConstants.TC_ENDBLOCKDATA -> {
                    rawRead()
                    return
                }
                ObjectStreamConstants.TC_BLOCKDATA, ObjectStreamConstants.TC_BLOCKDATALONG -> {
                    refillBlockData()
                }
                ObjectStreamConstants.TC_RESET -> {
                    rawRead()
                    resetHandles()
                }
                else -> {
                    // Consume any trailing embedded object written by custom writeObject
                    readObjectInternal(unshared = false)
                }
            }
        }
    }

    private fun readObjectInternal(unshared: Boolean): Any? {
        requireStream()
        if (unreadBlockBytes > 0) {
            throw StreamCorruptedException("Unread block data ($unreadBlockBytes bytes) before readObject()")
        }
        while (true) {
            val tc = rawReadUnsignedByte().toByte()
            when (tc) {
                ObjectStreamConstants.TC_RESET -> resetHandles()
                ObjectStreamConstants.TC_NULL -> return null
                ObjectStreamConstants.TC_REFERENCE -> {
                    val wireHandle = rawReadInt()
                    if (unshared) {
                        throw InvalidObjectException("cannot read back reference as unshared")
                    }
                    return resolveHandle(wireHandle)
                }
                ObjectStreamConstants.TC_STRING, ObjectStreamConstants.TC_LONGSTRING -> {
                    return readStringToken(tc, unshared)
                }
                ObjectStreamConstants.TC_ARRAY -> {
                    val desc = readClassDesc() ?: throw StreamCorruptedException("Null array ClassDesc")
                    return readArrayContent(desc, unshared)
                }
                ObjectStreamConstants.TC_OBJECT -> {
                    val desc = readClassDesc() ?: throw StreamCorruptedException("Null object ClassDesc")
                    return readOrdinaryObject(desc, unshared)
                }
                ObjectStreamConstants.TC_BLOCKDATA, ObjectStreamConstants.TC_BLOCKDATALONG -> {
                    throw StreamCorruptedException("Unexpected block data token when reading object")
                }
                else -> throw StreamCorruptedException("Unsupported serialized object tag: 0x${(tc.toInt() and 0xFF).toString(16)}")
            }
        }
    }

    private fun readArrayContent(desc: SerialClassDesc, unshared: Boolean): Any {
        val handleIdx = assignHandle(null, unshared)
        val size = rawReadInt()
        if (size < 0) throw StreamCorruptedException("Negative array size: $size")
        val arrayObj: Any = when (desc.name) {
            "[B" -> rawReadByteArrayChunked(size)
            "[Z" -> {
                val list = ArrayList<Boolean>(minOf(size, INITIAL_ELEMENT_CAP))
                repeat(size) { list.add(rawReadBoolean()) }
                list.toBooleanArray()
            }
            "[S" -> {
                val list = ArrayList<Short>(minOf(size, INITIAL_ELEMENT_CAP))
                repeat(size) { list.add(rawReadShort()) }
                list.toShortArray()
            }
            "[C" -> {
                val list = ArrayList<Char>(minOf(size, INITIAL_ELEMENT_CAP))
                repeat(size) { list.add(rawReadChar()) }
                list.toCharArray()
            }
            "[I" -> {
                val list = ArrayList<Int>(minOf(size, INITIAL_ELEMENT_CAP))
                repeat(size) { list.add(rawReadInt()) }
                list.toIntArray()
            }
            "[J" -> {
                val list = ArrayList<Long>(minOf(size, INITIAL_ELEMENT_CAP))
                repeat(size) { list.add(rawReadLong()) }
                list.toLongArray()
            }
            "[F" -> {
                val list = ArrayList<Float>(minOf(size, INITIAL_ELEMENT_CAP))
                repeat(size) { list.add(rawReadFloat()) }
                list.toFloatArray()
            }
            "[D" -> {
                val list = ArrayList<Double>(minOf(size, INITIAL_ELEMENT_CAP))
                repeat(size) { list.add(rawReadDouble()) }
                list.toDoubleArray()
            }
            else -> {
                if (!desc.name.startsWith("[")) {
                    throw InvalidClassException(desc.name, "Invalid array descriptor")
                }
                if (size <= INITIAL_ELEMENT_CAP) {
                    val arr = arrayOfNulls<Any>(size)
                    setHandleValue(handleIdx, arr, unshared)
                    for (i in 0 until size) {
                        arr[i] = readObjectInternal(unshared = false)
                    }
                    arr
                } else {
                    val list = ArrayList<Any?>(INITIAL_ELEMENT_CAP)
                    repeat(size) { list.add(readObjectInternal(unshared = false)) }
                    list.toTypedArray()
                }
            }
        }
        setHandleValue(handleIdx, arrayObj, unshared)
        return arrayObj
    }

    private fun collectHierarchy(leaf: SerialClassDesc): List<SerialClassDesc> {
        val chain = ArrayList<SerialClassDesc>(4)
        var cur: SerialClassDesc? = leaf
        while (cur != null) {
            chain.add(cur)
            cur = cur.superDesc
        }
        chain.reverse()
        return chain
    }

    private fun readSlotFields(slot: SerialClassDesc): Map<String, Any?> {
        if (slot.fields.isEmpty()) return emptyMap()
        val values = HashMap<String, Any?>(slot.fields.size)
        for (f in slot.fields) {
            val v: Any? = when (f.typeCode) {
                'Z' -> rawReadBoolean()
                'B' -> rawReadByte()
                'S' -> rawReadShort()
                'C' -> rawReadChar()
                'I' -> rawReadInt()
                'J' -> rawReadLong()
                'F' -> rawReadFloat()
                'D' -> rawReadDouble()
                'L', '[' -> readObjectInternal(unshared = false)
                else -> throw StreamCorruptedException("Unknown field typeCode '${f.typeCode}'")
            }
            values[f.name] = v
        }
        return values
    }

    private fun hasWriteMethod(slot: SerialClassDesc): Boolean =
        (slot.flags and ObjectStreamConstants.SC_WRITE_METHOD.toInt()) != 0

    private fun readOrdinaryObject(desc: SerialClassDesc, unshared: Boolean): Any {
        val handleIdx = assignHandle(null, unshared)
        val hierarchy = collectHierarchy(desc)

        return when (desc.name) {
            "java.lang.Boolean",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Character",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Float",
            "java.lang.Double" -> {
                var fieldValue: Any? = null
                for (slot in hierarchy) {
                    val fields = readSlotFields(slot)
                    if ("value" in fields) fieldValue = fields["value"]
                    if (hasWriteMethod(slot)) skipCustomDataToEndBlock()
                }
                val result = fieldValue ?: throw StreamCorruptedException("Missing value field for ${desc.name}")
                setHandleValue(handleIdx, result, unshared)
                result
            }
            "java.util.ArrayList" -> {
                val list = ArrayList<Any?>()
                setHandleValue(handleIdx, list, unshared)
                for (slot in hierarchy) {
                    val fields = readSlotFields(slot)
                    if (slot.name == "java.util.ArrayList") {
                        val size = (fields["size"] as? Int) ?: 0
                        if (size < 0) throw StreamCorruptedException("Negative ArrayList size: $size")
                        if (hasWriteMethod(slot)) {
                            readInt() // capacity in block data
                            repeat(size) {
                                list.add(readObjectInternal(unshared = false))
                            }
                            skipCustomDataToEndBlock()
                        }
                    } else if (hasWriteMethod(slot)) {
                        skipCustomDataToEndBlock()
                    }
                }
                list
            }
            "java.util.Arrays\$ArrayList" -> {
                var arr: Array<*>? = null
                for (slot in hierarchy) {
                    val fields = readSlotFields(slot)
                    if (slot.name == "java.util.Arrays\$ArrayList") {
                        arr = fields["a"] as? Array<*>
                    }
                    if (hasWriteMethod(slot)) skipCustomDataToEndBlock()
                }
                val list = ArrayList<Any?>(arr?.size ?: 0)
                if (arr != null) {
                    for (item in arr) list.add(item)
                }
                setHandleValue(handleIdx, list, unshared)
                list
            }
            "java.util.LinkedList" -> {
                val list = java.util.LinkedList<Any?>()
                setHandleValue(handleIdx, list, unshared)
                for (slot in hierarchy) {
                    readSlotFields(slot)
                    if (slot.name == "java.util.LinkedList" && hasWriteMethod(slot)) {
                        val size = readInt()
                        if (size < 0) throw StreamCorruptedException("Negative LinkedList size: $size")
                        repeat(size) {
                            list.add(readObjectInternal(unshared = false))
                        }
                        skipCustomDataToEndBlock()
                    } else if (hasWriteMethod(slot)) {
                        skipCustomDataToEndBlock()
                    }
                }
                list
            }
            "java.util.HashSet", "java.util.LinkedHashSet" -> {
                val set: MutableSet<Any?> = if (desc.name == "java.util.LinkedHashSet") {
                    kotlin.collections.LinkedHashSet()
                } else {
                    kotlin.collections.HashSet()
                }
                setHandleValue(handleIdx, set, unshared)
                for (slot in hierarchy) {
                    readSlotFields(slot)
                    if (slot.name == "java.util.HashSet" && hasWriteMethod(slot)) {
                        readInt() // capacity
                        readFloat() // loadFactor
                        val size = readInt()
                        if (size < 0) throw StreamCorruptedException("Negative HashSet size: $size")
                        repeat(size) {
                            set.add(readObjectInternal(unshared = false))
                        }
                        skipCustomDataToEndBlock()
                    } else if (hasWriteMethod(slot)) {
                        skipCustomDataToEndBlock()
                    }
                }
                set
            }
            "java.util.HashMap", "java.util.LinkedHashMap" -> {
                val entries = ArrayList<Pair<Any?, Any?>>()
                var accessOrder = false
                val placeholderMap: MutableMap<Any?, Any?> = if (desc.name == "java.util.LinkedHashMap") {
                    java.util.LinkedHashMap()
                } else {
                    kotlin.collections.HashMap()
                }
                setHandleValue(handleIdx, placeholderMap, unshared)
                for (slot in hierarchy) {
                    val fields = readSlotFields(slot)
                    if (slot.name == "java.util.HashMap" && hasWriteMethod(slot)) {
                        readInt() // buckets
                        val size = readInt()
                        if (size < 0) throw StreamCorruptedException("Negative HashMap size: $size")
                        repeat(size) {
                            val k = readObjectInternal(unshared = false)
                            val v = readObjectInternal(unshared = false)
                            entries.add(k to v)
                            placeholderMap[k] = v
                        }
                        skipCustomDataToEndBlock()
                    } else if (slot.name == "java.util.LinkedHashMap") {
                        accessOrder = (fields["accessOrder"] as? Boolean) ?: false
                        if (hasWriteMethod(slot)) skipCustomDataToEndBlock()
                    } else if (hasWriteMethod(slot)) {
                        skipCustomDataToEndBlock()
                    }
                }
                if (desc.name == "java.util.LinkedHashMap" && accessOrder) {
                    val lru = java.util.LinkedHashMap<Any?, Any?>(16, 0.75f, true)
                    for ((k, v) in entries) lru[k] = v
                    setHandleValue(handleIdx, lru, unshared)
                    lru
                } else {
                    placeholderMap
                }
            }
            "java.util.Date" -> {
                var fastTime = 0L
                for (slot in hierarchy) {
                    readSlotFields(slot)
                    if (slot.name == "java.util.Date" && hasWriteMethod(slot)) {
                        fastTime = readLong()
                        skipCustomDataToEndBlock()
                    } else if (hasWriteMethod(slot)) {
                        skipCustomDataToEndBlock()
                    }
                }
                val date = Date(fastTime)
                setHandleValue(handleIdx, date, unshared)
                date
            }
            "java.util.UUID" -> {
                var most = 0L
                var least = 0L
                for (slot in hierarchy) {
                    val fields = readSlotFields(slot)
                    if (slot.name == "java.util.UUID") {
                        most = (fields["mostSigBits"] as? Long) ?: 0L
                        least = (fields["leastSigBits"] as? Long) ?: 0L
                    }
                    if (hasWriteMethod(slot)) skipCustomDataToEndBlock()
                }
                val uuid = UUID(most, least)
                setHandleValue(handleIdx, uuid, unshared)
                uuid
            }
            "java.io.File" -> {
                var rawPath = ""
                var sepChar = File.separatorChar
                for (slot in hierarchy) {
                    val fields = readSlotFields(slot)
                    if (slot.name == "java.io.File") {
                        rawPath = (fields["path"] as? String) ?: ""
                        if (hasWriteMethod(slot)) {
                            sepChar = readChar()
                            skipCustomDataToEndBlock()
                        }
                    } else if (hasWriteMethod(slot)) {
                        skipCustomDataToEndBlock()
                    }
                }
                val normalized = if (sepChar != File.separatorChar) {
                    rawPath.replace(sepChar, File.separatorChar)
                } else {
                    rawPath
                }
                val file = File(normalized)
                setHandleValue(handleIdx, file, unshared)
                file
            }
            "java.net.URI" -> {
                var uriStr = ""
                for (slot in hierarchy) {
                    val fields = readSlotFields(slot)
                    if (slot.name == "java.net.URI") {
                        uriStr = (fields["string"] as? String) ?: ""
                    }
                    if (hasWriteMethod(slot)) skipCustomDataToEndBlock()
                }
                val uri = URI(uriStr)
                setHandleValue(handleIdx, uri, unshared)
                uri
            }
            "java.math.BigInteger" -> {
                var signum = 0
                var magnitude = ByteArray(0)
                for (slot in hierarchy) {
                    val fields = readSlotFields(slot)
                    if (slot.name == "java.math.BigInteger") {
                        signum = (fields["signum"] as? Int) ?: 0
                        magnitude = (fields["magnitude"] as? ByteArray) ?: ByteArray(0)
                    }
                    if (hasWriteMethod(slot)) skipCustomDataToEndBlock()
                }
                val bi = BigInteger(signum, magnitude)
                setHandleValue(handleIdx, bi, unshared)
                bi
            }
            "java.math.BigDecimal" -> {
                var scale = 0
                var intVal: BigInteger? = null
                for (slot in hierarchy) {
                    val fields = readSlotFields(slot)
                    if (slot.name == "java.math.BigDecimal") {
                        scale = (fields["scale"] as? Int) ?: 0
                        intVal = fields["intVal"] as? BigInteger
                    }
                    if (hasWriteMethod(slot)) skipCustomDataToEndBlock()
                }
                val bd = BigDecimal(intVal ?: BigInteger.ZERO, scale)
                setHandleValue(handleIdx, bd, unshared)
                bd
            }
            "java.util.Locale" -> {
                var language = ""
                var country = ""
                var variant = ""
                for (slot in hierarchy) {
                    val fields = readSlotFields(slot)
                    if (slot.name == "java.util.Locale") {
                        language = (fields["language"] as? String) ?: ""
                        country = (fields["country"] as? String) ?: ""
                        variant = (fields["variant"] as? String) ?: ""
                    }
                    if (hasWriteMethod(slot)) skipCustomDataToEndBlock()
                }
                val locale = Locale(language, country, variant)
                setHandleValue(handleIdx, locale, unshared)
                locale
            }
            else -> {
                throw InvalidClassException(
                    desc.name,
                    "Arbitrary user-defined Serializable classes cannot be reflectively instantiated on non-JVM targets; " +
                        "use built-in JDK types, Externalizable manually, kotlinx.serialization, or @Parcelize"
                )
            }
        }
    }

    actual override fun read(): Int {
        if (rawIn == null) return -1
        return readBlockByteOrEof()
    }

    override fun read(b: ByteArray): Int = read(b, 0, b.size)

    actual override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (rawIn == null) return -1
        if (off < 0 || len < 0 || off + len > b.size) throw IndexOutOfBoundsException()
        if (len == 0) return 0
        if (!refillBlockData()) return -1
        val toRead = minOf(len, unreadBlockBytes)
        var readCount = 0
        if (peekedByte >= 0) {
            b[off] = peekedByte.toByte()
            peekedByte = -1
            readCount = 1
        }
        if (readCount < toRead) {
            val n = requireStream().read(b, off + readCount, toRead - readCount)
            if (n > 0) {
                readCount += n
            } else if (readCount == 0) {
                return -1
            }
        }
        unreadBlockBytes -= readCount
        return readCount
    }

    override fun skip(n: Long): Long {
        if (rawIn == null || n <= 0L) return 0L
        var skipped = 0L
        val buf = ByteArray(minOf(n, 1024L).toInt())
        while (skipped < n) {
            val want = minOf(n - skipped, buf.size.toLong()).toInt()
            val count = read(buf, 0, want)
            if (count <= 0) break
            skipped += count
        }
        return skipped
    }

    actual override fun available(): Int {
        if (rawIn == null) return 0
        refillBlockData()
        return unreadBlockBytes
    }

    actual override fun close() {
        rawIn?.close()
    }

    actual override fun readBoolean(): Boolean = readBlockUnsignedByte() != 0

    actual override fun readByte(): Byte = readBlockUnsignedByte().toByte()

    actual override fun readUnsignedByte(): Int = readBlockUnsignedByte()

    actual override fun readShort(): Short {
        val ch1 = readBlockUnsignedByte()
        val ch2 = readBlockUnsignedByte()
        return ((ch1 shl 8) or ch2).toShort()
    }

    actual override fun readUnsignedShort(): Int {
        val ch1 = readBlockUnsignedByte()
        val ch2 = readBlockUnsignedByte()
        return (ch1 shl 8) or ch2
    }

    actual override fun readChar(): Char = readUnsignedShort().toChar()

    actual override fun readInt(): Int {
        val ch1 = readBlockUnsignedByte()
        val ch2 = readBlockUnsignedByte()
        val ch3 = readBlockUnsignedByte()
        val ch4 = readBlockUnsignedByte()
        return (ch1 shl 24) or (ch2 shl 16) or (ch3 shl 8) or ch4
    }

    actual override fun readLong(): Long {
        val hi = readInt().toLong()
        val lo = readInt().toLong() and 0xFFFFFFFFL
        return (hi shl 32) or lo
    }

    actual override fun readFloat(): Float = Float.fromBits(readInt())

    actual override fun readDouble(): Double = Double.fromBits(readLong())

    actual override fun readFully(b: ByteArray) = readFully(b, 0, b.size)

    actual override fun readFully(b: ByteArray, off: Int, len: Int) {
        if (len < 0 || off < 0 || off + len > b.size) throw IndexOutOfBoundsException()
        var n = 0
        while (n < len) {
            val count = read(b, off + n, len - n)
            if (count < 0) throw EOFException()
            n += count
        }
    }

    actual override fun skipBytes(n: Int): Int {
        if (n <= 0) return 0
        return skip(n.toLong()).toInt()
    }

    actual override fun readLine(): String? {
        val sb = StringBuilder()
        var c = readBlockByteOrEof()
        if (c == -1) return null
        while (c != -1 && c != '\n'.code) {
            if (c == '\r'.code) {
                if (unreadBlockBytes > 0 || refillBlockData()) {
                    val next = rawPeek()
                    if (next == '\n'.code) {
                        rawRead()
                        unreadBlockBytes--
                    }
                }
                break
            }
            sb.append((c and 0xFF).toChar())
            c = readBlockByteOrEof()
        }
        return sb.toString()
    }

    actual override fun readUTF(): String {
        val utflen = readUnsignedShort()
        val bytes = ByteArray(utflen)
        readFully(bytes, 0, utflen)
        return ModifiedUtf8.decode(bytes, 0, utflen)
    }

    companion object {
        private const val INITIAL_CHUNK_LIMIT = 65536
        private const val INITIAL_ELEMENT_CAP = 1024
    }
}
