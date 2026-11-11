package jako.test.io

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.CharConversionException
import java.io.Externalizable
import java.io.File
import java.io.IOException
import java.io.InvalidClassException
import java.io.InvalidObjectException
import java.io.NotActiveException
import java.io.NotSerializableException
import java.io.ObjectInput
import java.io.ObjectInputStream
import java.io.ObjectOutput
import java.io.ObjectOutputStream
import java.io.ObjectStreamConstants
import java.io.ObjectStreamException
import java.io.StreamCorruptedException
import java.io.StreamTokenizer
import java.io.StringReader
import java.io.UTFDataFormatException
import java.io.UncheckedIOException
import java.io.UnsupportedEncodingException
import java.io.WriteAbortedException
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.util.Date
import java.util.LinkedHashMap
import java.util.LinkedList
import java.util.Locale
import java.util.UUID
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet
import java.util.stream.Collectors
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class IoSerializationAndStreamTokenizerTest {

    private class SampleExternalizable(var id: Int = 0, var label: String = "") : Externalizable {
        override fun writeExternal(out: ObjectOutput) {
            out.writeInt(id)
            out.writeUTF(label)
        }

        override fun readExternal(inStream: ObjectInput) {
            id = inStream.readInt()
            label = inStream.readUTF()
        }
    }

    @Test
    fun testObjectStreamConstantsAndExternalizable() {
        assertEquals(0xaced.toShort(), ObjectStreamConstants.STREAM_MAGIC)
        assertEquals(5.toShort(), ObjectStreamConstants.STREAM_VERSION)
        assertEquals(1, ObjectStreamConstants.PROTOCOL_VERSION_1)
        assertEquals(2, ObjectStreamConstants.PROTOCOL_VERSION_2)

        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        val original = SampleExternalizable(77, "jako-ext")
        original.writeExternal(oos)
        oos.writeObject("tail")
        oos.close()

        val ois = ObjectInputStream(ByteArrayInputStream(baos.toByteArray()))
        val restored = SampleExternalizable()
        restored.readExternal(ois)
        assertEquals(77, restored.id)
        assertEquals("jako-ext", restored.label)
        assertEquals("tail", ois.readObject())
        ois.close()

        assertFailsWith<StreamCorruptedException> {
            ObjectInputStream(ByteArrayInputStream(byteArrayOf(0x00, 0x01, 0x02, 0x03)))
        }
    }

    @Test
    fun testObjectStreamRoundTripScalarsAndCollections() {
        val withNull = ArrayList<Any?>()
        withNull.add(null)
        withNull.add("a")
        val nullValueMap = LinkedHashMap<String?, Any?>()
        nullValueMap.put("k", null)

        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(null)
        oos.writeObject("")
        oos.writeObject("你好")
        oos.writeObject(0)
        oos.writeObject(-1)
        oos.writeObject(Int.MIN_VALUE)
        oos.writeObject(Long.MIN_VALUE)
        oos.writeObject(false)
        oos.writeObject(Double.NaN)
        oos.writeObject(-0.0)
        oos.writeObject(ArrayList<Any?>())
        oos.writeObject(withNull)
        oos.writeObject(byteArrayOf())
        oos.writeObject(byteArrayOf(-1, 0, 127))
        oos.writeObject(LinkedHashMap<String?, Any?>())
        oos.writeObject(nullValueMap)
        oos.close()

        val ois = ObjectInputStream(ByteArrayInputStream(baos.toByteArray()))
        assertNull(ois.readObject())
        assertEquals("", ois.readObject() as String)
        assertEquals("你好", ois.readObject() as String)
        assertEquals(0, ois.readObject() as Int)
        assertEquals(-1, ois.readObject() as Int)
        assertEquals(Int.MIN_VALUE, ois.readObject() as Int)
        assertEquals(Long.MIN_VALUE, ois.readObject() as Long)
        assertEquals(false, ois.readObject() as Boolean)

        val nan = ois.readObject() as Double
        assertTrue(nan.isNaN())

        val negativeZero = ois.readObject() as Double
        assertEquals((-0.0).toRawBits(), negativeZero.toRawBits())

        val emptyList = ois.readObject() as List<*>
        assertEquals(0, emptyList.size)

        val restoredList = ois.readObject() as List<*>
        assertEquals(2, restoredList.size)
        assertNull(restoredList[0])
        assertEquals("a", restoredList[1])

        assertContentEquals(byteArrayOf(), ois.readObject() as ByteArray)
        assertContentEquals(byteArrayOf(-1, 0, 127), ois.readObject() as ByteArray)

        val emptyMap = ois.readObject() as Map<*, *>
        assertEquals(0, emptyMap.size)

        @Suppress("UNCHECKED_CAST")
        val restoredMap = ois.readObject() as Map<String?, Any?>
        assertEquals(1, restoredMap.size)
        assertTrue(restoredMap.containsKey("k"))
        assertNull(restoredMap["k"])
        ois.close()
    }

    @Test
    fun testWriteObjectRejectsNonSerializableLocalClass() {
        class LocalPayload(val value: Int)

        val oos = ObjectOutputStream(ByteArrayOutputStream())
        assertFailsWith<NotSerializableException> {
            oos.writeObject(LocalPayload(1))
        }
        oos.close()
    }

    private fun hexToBytes(hex: String): ByteArray {
        check(hex.length % 2 == 0)
        return ByteArray(hex.length / 2) { i ->
            hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    private fun bytesToHex(bytes: ByteArray): String = buildString(bytes.size * 2) {
        for (b in bytes) {
            val v = b.toInt() and 0xFF
            append(v.toString(16).padStart(2, '0'))
        }
    }

    private fun serializeSingle(value: Any?): ByteArray {
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { it.writeObject(value) }
        return baos.toByteArray()
    }

    private fun deserializeSingle(bytes: ByteArray): Any? =
        ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() }

    @Test
    fun testJvmBinaryProtocolGoldenWireCompatibility() {
        // Golden byte streams generated by OpenJDK 8 ObjectOutputStream (0xACED 0x0005)
        val goldenInteger42 =
            "aced0005737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c756578720010" +
                "6a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000002a"
        assertEquals(goldenInteger42, bytesToHex(serializeSingle(42)))
        assertEquals(42, deserializeSingle(hexToBytes(goldenInteger42)))

        val goldenArrayList =
            "aced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a65787000" +
                "00000277040000000274000161737200116a6176612e6c616e672e496e746567657212e2a0a4f7818738020001490005" +
                "76616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b02000078700000000178"
        val listInput = ArrayList<Any?>().apply { add("a"); add(1) }
        assertEquals(goldenArrayList, bytesToHex(serializeSingle(listInput)))
        assertEquals(listOf<Any?>("a", 1), deserializeSingle(hexToBytes(goldenArrayList)))

        val goldenLinkedList =
            "aced0005737200146a6176612e7574696c2e4c696e6b65644c6973740c29535d4a60882203000078707704000000017400016178"
        val linkedListInput = LinkedList<String>().apply { add("a") }
        assertEquals(goldenLinkedList, bytesToHex(serializeSingle(linkedListInput)))
        assertEquals(listOf("a"), deserializeSingle(hexToBytes(goldenLinkedList)))

        val goldenLinkedHashSet =
            "aced0005737200176a6176612e7574696c2e4c696e6b656448617368536574d86cd75a95dd2a1e020000787200116a61" +
                "76612e7574696c2e48617368536574ba44859596b8b7340300007870770c000000103f400000000000017400017878"
        val lhsInput = LinkedHashSet<String>().apply { add("x") }
        assertEquals(goldenLinkedHashSet, bytesToHex(serializeSingle(lhsInput)))
        assertEquals(setOf("x"), deserializeSingle(hexToBytes(goldenLinkedHashSet)))

        val goldenHashSet =
            "aced0005737200116a6176612e7574696c2e48617368536574ba44859596b8b7340300007870770c000000103f400000000000017400017878"
        assertEquals(setOf("x"), deserializeSingle(hexToBytes(goldenHashSet)))

        val goldenLinkedHashMap =
            "aced0005737200176a6176612e7574696c2e4c696e6b6564486173684d617034c04e5c106cc0fb0200015a000b616363" +
                "6573734f72646572787200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f616446" +
                "6163746f724900097468726573686f6c6478703f4000000000000c770800000010000000017400016b737200116a6176" +
                "612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e" +
                "756d62657286ac951d0b94e08b0200007870000000077800"
        val lhmInput = LinkedHashMap<String, Int>().apply { put("k", 7) }
        assertEquals(goldenLinkedHashMap, bytesToHex(serializeSingle(lhmInput)))
        assertEquals(mapOf("k" to 7), deserializeSingle(hexToBytes(goldenLinkedHashMap)))

        val goldenDate = "aced00057372000e6a6176612e7574696c2e44617465686a81014b597419030000787077080000018bcfe5680078"
        assertEquals(goldenDate, bytesToHex(serializeSingle(Date(1700000000000L))))
        assertEquals(Date(1700000000000L), deserializeSingle(hexToBytes(goldenDate)))

        val goldenUuid =
            "aced00057372000e6a6176612e7574696c2e55554944bc9903f7986d852f0200024a000c6c6561737453696742697473" +
                "4a000b6d6f737453696742697473787000000000000000020000000000000001"
        assertEquals(goldenUuid, bytesToHex(serializeSingle(UUID(1L, 2L))))
        assertEquals(UUID(1L, 2L), deserializeSingle(hexToBytes(goldenUuid)))

        val goldenUri =
            "aced00057372000c6a6176612e6e65742e555249ac01782e439e49ab0300014c0006737472696e677400124c6a617661" +
                "2f6c616e672f537472696e673b787074001368747470733a2f2f6578616d706c652e636f6d78"
        assertEquals(goldenUri, bytesToHex(serializeSingle(URI.create("https://example.com"))))
        assertEquals(URI.create("https://example.com"), deserializeSingle(hexToBytes(goldenUri)))

        val goldenBigInt =
            "aced0005737200146a6176612e6d6174682e426967496e74656765728cfc9f1fa93bfb1d030006490008626974436f75" +
                "6e744900096269744c656e67746849001366697273744e6f6e7a65726f427974654e756d49000c6c6f77657374536574" +
                "4269744900067369676e756d5b00096d61676e69747564657400025b42787200106a6176612e6c616e672e4e756d6265" +
                "7286ac951d0b94e08b0200007870fffffffffffffffffffffffefffffffe00000001757200025b42acf317f8060854e0" +
                "020000787000000004499602d278"
        assertEquals(goldenBigInt, bytesToHex(serializeSingle(BigInteger("1234567890"))))
        assertEquals(BigInteger("1234567890"), deserializeSingle(hexToBytes(goldenBigInt)))

        val goldenBigDec =
            "aced0005737200146a6176612e6d6174682e426967446563696d616c54c71557f981284f0300024900057363616c654c" +
                "0006696e7456616c7400164c6a6176612f6d6174682f426967496e74656765723b787200106a6176612e6c616e672e4e" +
                "756d62657286ac951d0b94e08b020000787000000002737200146a6176612e6d6174682e426967496e74656765728cfc" +
                "9f1fa93bfb1d030006490008626974436f756e744900096269744c656e67746849001366697273744e6f6e7a65726f42" +
                "7974654e756d49000c6c6f776573745365744269744900067369676e756d5b00096d61676e69747564657400025b4278" +
                "71007e0002fffffffffffffffffffffffefffffffe00000001757200025b42acf317f8060854e002000078700000000204d27878"
        assertEquals(goldenBigDec, bytesToHex(serializeSingle(BigDecimal("12.34"))))
        assertEquals(BigDecimal("12.34"), deserializeSingle(hexToBytes(goldenBigDec)))

        val goldenLocale =
            "aced0005737200106a6176612e7574696c2e4c6f63616c657ef811609c30f9ec03000649000868617368636f64654c00" +
                "07636f756e7472797400124c6a6176612f6c616e672f537472696e673b4c000a657874656e73696f6e7371007e00014c" +
                "00086c616e677561676571007e00014c000673637269707471007e00014c000776617269616e7471007e00017870ffff" +
                "ffff7400025553740000740002656e71007e000471007e000478"
        assertEquals(goldenLocale, bytesToHex(serializeSingle(Locale("en", "US"))))
        assertEquals(Locale("en", "US"), deserializeSingle(hexToBytes(goldenLocale)))

        val goldenFile =
            "aced00057372000c6a6176612e696f2e46696c65042da4450e0de4ff0300014c0004706174687400124c6a6176612f6c" +
                "616e672f537472696e673b78707400062f746d702f617702002f78"
        val restoredFile = deserializeSingle(hexToBytes(goldenFile)) as File
        assertEquals("/tmp/a", restoredFile.path.replace('\\', '/'))
    }

    @Test
    fun testObjectStreamReferencesResetUnsharedArraysAndLargeBlocks() {
        val sharedList = arrayListOf("item")
        val cyclic = ArrayList<Any?>()
        cyclic.add("head")
        cyclic.add(cyclic)

        val unsharedList = arrayListOf("u1")
        val bigBlock = ByteArray(1500) { (it and 0x7F).toByte() }
        val longString = "x".repeat(70_000)

        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { oos ->
            oos.useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_2)
            assertFailsWith<NotActiveException> { oos.defaultWriteObject() }

            oos.writeObject(sharedList)
            oos.writeObject(sharedList) // TC_REFERENCE -> same instance
            oos.writeObject(cyclic)
            oos.reset()
            oos.writeObject(sharedList) // after TC_RESET -> distinct instance
            oos.writeUnshared(unsharedList)
            oos.writeObject(unsharedList) // must not reference unshared handle

            oos.writeObject(booleanArrayOf(true, false, true))
            oos.writeObject(shortArrayOf(-10, 20))
            oos.writeObject(charArrayOf('J', 'K'))
            oos.writeObject(intArrayOf(1, 2, 3))
            oos.writeObject(longArrayOf(-99L, 100L))
            oos.writeObject(floatArrayOf(1.5f, -2.5f))
            oos.writeObject(doubleArrayOf(3.14, -6.28))
            oos.writeObject(arrayOf<Any?>("one", 2, null))
            oos.writeObject(HashSet<String>().apply { add("hs") })
            oos.writeObject(HashMap<String, Int>().apply { put("hm", 9) })
            oos.writeObject(BigInteger("-98765432109876543210"))
            oos.writeObject(BigInteger.ZERO)

            oos.write(bigBlock) // > 1024 bytes -> TC_BLOCKDATALONG + TC_BLOCKDATA
            oos.writeObject(longString) // > 65535 bytes -> TC_LONGSTRING
        }

        ObjectInputStream(ByteArrayInputStream(baos.toByteArray())).use { ois ->
            assertFailsWith<NotActiveException> { ois.defaultReadObject() }

            val first = ois.readObject() as List<*>
            val second = ois.readObject() as List<*>
            assertSame(first, second)

            val restoredCyclic = ois.readObject() as List<*>
            assertEquals("head", restoredCyclic[0])
            assertSame(restoredCyclic, restoredCyclic[1])

            val afterReset = ois.readObject() as List<*>
            assertEquals(first, afterReset)
            assertTrue(first !== afterReset)

            val uFirst = ois.readUnshared() as List<*>
            val uSecond = ois.readObject() as List<*>
            assertEquals(uFirst, uSecond)
            assertTrue(uFirst !== uSecond)

            assertContentEquals(booleanArrayOf(true, false, true), ois.readObject() as BooleanArray)
            assertContentEquals(shortArrayOf(-10, 20), ois.readObject() as ShortArray)
            assertContentEquals(charArrayOf('J', 'K'), ois.readObject() as CharArray)
            assertContentEquals(intArrayOf(1, 2, 3), ois.readObject() as IntArray)
            assertContentEquals(longArrayOf(-99L, 100L), ois.readObject() as LongArray)
            assertContentEquals(floatArrayOf(1.5f, -2.5f), ois.readObject() as FloatArray)
            assertContentEquals(doubleArrayOf(3.14, -6.28), ois.readObject() as DoubleArray)
            @Suppress("UNCHECKED_CAST")
            val objArr = ois.readObject() as Array<Any?>
            assertEquals(3, objArr.size)
            assertEquals("one", objArr[0])
            assertEquals(2, objArr[1])
            assertNull(objArr[2])

            assertEquals(setOf("hs"), ois.readObject())
            assertEquals(mapOf("hm" to 9), ois.readObject())
            assertEquals(BigInteger("-98765432109876543210"), ois.readObject())
            assertEquals(BigInteger.ZERO, ois.readObject())

            val readBackBlock = ByteArray(1500)
            ois.readFully(readBackBlock)
            assertContentEquals(bigBlock, readBackBlock)

            assertEquals(longString, ois.readObject())
        }
    }

    @Test
    fun testStreamTokenizer() {
        val input = """
            // single-line comment
            hello 42 -3.5 "quoted\nvalue"
            /* multi-line
               comment */
            world ;
        """.trimIndent()

        val tokenizer = StreamTokenizer(StringReader(input))
        tokenizer.slashSlashComments(true)
        tokenizer.slashStarComments(true)
        tokenizer.ordinaryChar(';'.code)

        assertEquals(StreamTokenizer.TT_WORD, tokenizer.nextToken())
        assertEquals("hello", tokenizer.sval)

        assertEquals(StreamTokenizer.TT_NUMBER, tokenizer.nextToken())
        assertEquals(42.0, tokenizer.nval)

        assertEquals(StreamTokenizer.TT_NUMBER, tokenizer.nextToken())
        assertEquals(-3.5, tokenizer.nval)

        assertEquals('"'.code, tokenizer.nextToken())
        assertEquals("quoted\nvalue", tokenizer.sval)

        tokenizer.pushBack()
        assertEquals('"'.code, tokenizer.nextToken())
        assertEquals("quoted\nvalue", tokenizer.sval)

        assertEquals(StreamTokenizer.TT_WORD, tokenizer.nextToken())
        assertEquals("world", tokenizer.sval)
        assertTrue(tokenizer.lineno() >= 4)

        assertEquals(';'.code, tokenizer.nextToken())
        assertEquals(StreamTokenizer.TT_EOF, tokenizer.nextToken())
    }

    @Test
    fun testEmptyStreamTokenizerReturnsEof() {
        val tokenizer = StreamTokenizer(StringReader(""))
        assertEquals(StreamTokenizer.TT_EOF, tokenizer.nextToken())
    }

    @Test
    fun testBufferedReaderLinesAndFileSpace() {
        val br = BufferedReader(StringReader("first\nsecond\nthird"))
        val lines = br.lines().collect(Collectors.toList<String>())
        assertEquals(listOf("first", "second", "third"), lines)
        br.close()

        val roots = File.listRoots()
        assertTrue(roots.isNotEmpty())

        val currentDir = File(".")
        assertTrue(currentDir.totalSpace > 0L)
        assertTrue(currentDir.freeSpace > 0L)
        assertTrue(currentDir.usableSpace > 0L)
        assertTrue(currentDir.usableSpace <= currentDir.totalSpace)

        val missing = File("/non_existent_jako_partition_path_987654321")
        assertEquals(0L, missing.totalSpace)
        assertEquals(0L, missing.freeSpace)
        assertEquals(0L, missing.usableSpace)
    }

    @Test
    fun testIoExceptionsHierarchy() {
        val ioCause = IOException("disk full")
        val uio = UncheckedIOException("wrapped", ioCause)
        assertSame(ioCause, uio.cause)

        assertTrue(UnsupportedEncodingException("UTF-99") is IOException)
        assertTrue(UTFDataFormatException("bad utf") is IOException)
        assertTrue(CharConversionException("bad char") is IOException)

        val ice = InvalidClassException("com.Foo", "incompatible uid")
        assertTrue(ice is ObjectStreamException)
        assertEquals("com.Foo", ice.classname)
        assertEquals("com.Foo; incompatible uid", ice.message)

        assertTrue(NotSerializableException("com.Bar") is ObjectStreamException)
        assertTrue(InvalidObjectException("invalid state") is ObjectStreamException)
        assertTrue(StreamCorruptedException("bad magic") is ObjectStreamException)
        assertTrue(NotActiveException("not active") is ObjectStreamException)

        val detail = IllegalStateException("aborted")
        val wae = WriteAbortedException("write failed", detail)
        assertSame(detail, wae.detail)
        assertSame(detail, wae.cause)
    }
}
