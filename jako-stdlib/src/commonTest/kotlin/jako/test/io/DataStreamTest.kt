package jako.test.io

import java.io.*

import kotlin.test.*

class DataStreamTest {

    @Test
    fun testDataInputAndOutputStream() {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        dos.writeBoolean(true)
        dos.writeByte(42)
        dos.writeShort(1234)
        dos.writeInt(987654321)
        dos.writeLong(1234567890123456789L)
        dos.writeFloat(3.14159f)
        dos.writeDouble(2.718281828459)
        dos.writeUTF("UTF-8 Binary Data Test 🚀")
        dos.flush()

        val bytes = baos.toByteArray()
        val bais = ByteArrayInputStream(bytes)
        val dis = DataInputStream(bais)

        assertTrue(dis.readBoolean())
        assertEquals(42.toByte(), dis.readByte())
        assertEquals(1234.toShort(), dis.readShort())
        assertEquals(987654321, dis.readInt())
        assertEquals(1234567890123456789L, dis.readLong())
        assertEquals(3.14159f, dis.readFloat(), 0.0001f)
        assertEquals(2.718281828459, dis.readDouble(), 0.0000001)
        assertEquals("UTF-8 Binary Data Test 🚀", dis.readUTF())

        dis.close()
        dos.close()
    }
}
