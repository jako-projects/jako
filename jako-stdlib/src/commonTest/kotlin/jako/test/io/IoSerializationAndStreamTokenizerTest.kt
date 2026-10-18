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
import java.util.stream.Collectors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
