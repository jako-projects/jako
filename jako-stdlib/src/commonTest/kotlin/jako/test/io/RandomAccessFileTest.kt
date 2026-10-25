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
