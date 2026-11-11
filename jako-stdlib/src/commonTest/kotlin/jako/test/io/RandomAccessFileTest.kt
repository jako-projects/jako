package jako.test.io

import java.io.*
import kotlin.test.*

class RandomAccessFileTest {

    private val tmpBase: File
        get() = File(java.lang.System.getProperty("java.io.tmpdir") ?: "/tmp")

    @Test
    fun testRandomAccessFileReadWriteSeek() {
        val tempFile = File(tmpBase, "raf_test_${java.lang.System.currentTimeMillis()}.bin")
        if (tempFile.exists()) tempFile.delete()

        try {
            val raf = RandomAccessFile(tempFile, "rw")
            raf.write(65) // 'A'
            raf.write(66) // 'B'
            raf.write(byteArrayOf(67, 68, 69)) // 'C', 'D', 'E'

            assertEquals(5L, raf.length())
            assertEquals(5L, raf.getFilePointer())

            raf.seek(0L)
            assertEquals(0L, raf.getFilePointer())
            assertEquals(65, raf.read())

            raf.seek(3L)
            assertEquals(68, raf.read()) // 'D'

            raf.close()
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    @Test
    fun testRandomAccessFileDataInputOutput() {
        val tempFile = File(tmpBase, "raf_data_test_${java.lang.System.currentTimeMillis()}.bin")
        if (tempFile.exists()) tempFile.delete()

        try {
            RandomAccessFile(tempFile, "rw").use { raf ->
                val out: DataOutput = raf
                out.writeBoolean(true)
                out.writeByte(0x7F)
                out.writeShort(0x1234)
                out.writeChar('K'.code)
                out.writeInt(0x12345678)
                out.writeLong(0x0102030405060708L)
                out.writeFloat(3.25f)
                out.writeDouble(2.718281828)
                out.writeUTF("Hello jako RAF!")
                out.writeBytes("line1\nline2\r\n")

                raf.seek(0L)
                val input: DataInput = raf
                assertTrue(input.readBoolean())
                assertEquals(0x7F.toByte(), input.readByte())
                assertEquals(0x1234.toShort(), input.readShort())
                assertEquals('K', input.readChar())
                assertEquals(0x12345678, input.readInt())
                assertEquals(0x0102030405060708L, input.readLong())
                assertEquals(3.25f, input.readFloat())
                assertEquals(2.718281828, input.readDouble())
                assertEquals("Hello jako RAF!", input.readUTF())
                assertEquals("line1", input.readLine())
                assertEquals("line2", input.readLine())
                assertNull(input.readLine())
            }
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    @Test
    fun testRandomAccessFileModifiedUtf8AndDataStreamInterop() {
        val tempFile = File(tmpBase, "raf_mutf8_${java.lang.System.currentTimeMillis()}.bin")
        if (tempFile.exists()) tempFile.delete()

        try {
            val tricky = "A\u0000B\u4F60\u597D\uD83D\uDE80Z"
            RandomAccessFile(tempFile, "rw").use { raf ->
                raf.writeUTF(tricky)
                raf.seek(0L)
                // Embedded NUL (\u0000) in Modified UTF-8 must be encoded as 0xC0 0x80 (2 bytes),
                // and surrogate pair \uD83D\uDE80 must be encoded as two 3-byte CESU-8 sequences (6 bytes):
                // 'A'(1) + NUL(2) + 'B'(1) + '你'(3) + '好'(3) + 🚀(6) + 'Z'(1) = 17 bytes
                val encodedLen = raf.readUnsignedShort()
                assertEquals(17, encodedLen)
                raf.seek(0L)
                assertEquals(tricky, raf.readUTF())
            }

            // DataInputStream must decode bytes written by RandomAccessFile.writeUTF
            DataInputStream(FileInputStream(tempFile)).use { dis ->
                assertEquals(tricky, dis.readUTF())
            }

            // RandomAccessFile.readUTF must decode bytes written by DataOutputStream.writeUTF
            DataOutputStream(FileOutputStream(tempFile)).use { dos ->
                dos.writeUTF(tricky)
            }
            RandomAccessFile(tempFile, "r").use { raf ->
                assertEquals(tricky, raf.readUTF())
            }

            // Malformed Modified UTF-8 (4-byte standard UTF-8 0xF0 prefix) must throw UTFDataFormatException
            RandomAccessFile(tempFile, "rw").use { raf ->
                raf.setLength(0L)
                raf.writeShort(4)
                raf.write(byteArrayOf(0xF0.toByte(), 0x9F.toByte(), 0x9A.toByte(), 0x80.toByte()))
                raf.seek(0L)
                assertFailsWith<UTFDataFormatException> {
                    raf.readUTF()
                }
            }

            // > 65535 Modified UTF-8 bytes must throw UTFDataFormatException
            RandomAccessFile(tempFile, "rw").use { raf ->
                val tooLong = "a".repeat(65536)
                assertFailsWith<UTFDataFormatException> {
                    raf.writeUTF(tooLong)
                }
            }
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    @Test
    fun testRandomAccessFileSetLengthAndReadOnlyMode() {
        val tempFile = File(tmpBase, "raf_setlen_${java.lang.System.currentTimeMillis()}.bin")
        if (tempFile.exists()) tempFile.delete()

        try {
            RandomAccessFile(tempFile, "rw").use { raf ->
                raf.write(byteArrayOf(1, 2, 3, 4, 5, 6))
                assertEquals(6L, raf.length())

                // Grow: file pointer <= newLength is preserved, extended bytes are zero-filled
                raf.seek(2L)
                raf.setLength(10L)
                assertEquals(10L, raf.length())
                assertEquals(2L, raf.getFilePointer())
                raf.seek(6L)
                val tail = ByteArray(4)
                raf.readFully(tail)
                assertContentEquals(byteArrayOf(0, 0, 0, 0), tail)

                // Shrink while pointer > newLength: pointer clamped to newLength
                raf.seek(9L)
                raf.setLength(4L)
                assertEquals(4L, raf.length())
                assertEquals(4L, raf.getFilePointer())

                // Shrink while pointer <= newLength: pointer preserved
                raf.seek(1L)
                raf.setLength(3L)
                assertEquals(3L, raf.length())
                assertEquals(1L, raf.getFilePointer())
                assertEquals(2, raf.read())
                // Interleaved write immediately after read without explicit seek
                raf.write(99)
                raf.seek(0L)
                val all = ByteArray(3)
                raf.readFully(all)
                assertContentEquals(byteArrayOf(1, 2, 99), all)

                // Negative length throws IOException
                assertFailsWith<IOException> {
                    raf.setLength(-1L)
                }
            }

            // Read-only ("r") mode must reject write and setLength with IOException
            RandomAccessFile(tempFile, "r").use { ro ->
                assertEquals(3L, ro.length())
                assertFailsWith<IOException> {
                    ro.write(1)
                }
                assertFailsWith<IOException> {
                    ro.write(byteArrayOf(1, 2))
                }
                assertFailsWith<IOException> {
                    ro.setLength(0L)
                }
                assertEquals(3L, ro.length())
            }
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    @Test
    fun testObjectStreamDataAndObjectRoundTrip() {
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { oos ->
            val dataOut: DataOutput = oos
            dataOut.writeInt(42)
            dataOut.writeUTF("header-meta")
            oos.writeObject("jako-object")
            oos.writeObject(null)
            oos.writeObject(arrayListOf("a", "b", "c"))
            val map = LinkedHashMap<String, Int>()
            map["one"] = 1
            map["two"] = 2
            oos.writeObject(map)
            oos.flush()
        }

        val bytes = baos.toByteArray()
        ObjectInputStream(ByteArrayInputStream(bytes)).use { ois ->
            val dataIn: DataInput = ois
            assertEquals(42, dataIn.readInt())
            assertEquals("header-meta", dataIn.readUTF())
            assertEquals("jako-object", ois.readObject())
            assertNull(ois.readObject())
            assertEquals(listOf("a", "b", "c"), ois.readObject())
            @Suppress("UNCHECKED_CAST")
            val restoredMap = ois.readObject() as Map<String, Int>
            assertEquals(mapOf("one" to 1, "two" to 2), restoredMap)
        }
    }
}
